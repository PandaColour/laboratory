import os
from pathlib import Path
from openai import OpenAI

def scan_java_files(model_path):
    model_path = model_path + "/src/main"
    for root, dirs, files in os.walk(model_path):
        for file in files:
            if file.endswith(".java"):
                file_path = os.path.join(root, file)
                test_file_path = file_path.replace("/main", "/test")
                test_file_path = test_file_path.replace(".java", "Test.java")

                if os.path.exists(test_file_path):
                    continue

                with open(file_path, "r", encoding="utf-8") as f:
                    content = f.read()

                if "public interface" in content:
                    continue

                if "public @interface" in content:
                    continue

                lines = content.splitlines()
                if len(lines) < 5:
                    continue

                if (lines[0].startswith("//") and
                        lines[1].startswith("//") and
                        lines[2].startswith("//")):
                    continue

                Path(test_file_path).parent.mkdir(parents=True, exist_ok=True)
                unit_test_content = generate_test_cases(content=content)
                with open(test_file_path, "w", encoding="utf-8") as f:
                    f.write(unit_test_content)

                print(test_file_path)
                print("生成单元测试成功!")
def generate_test_cases(content):
    current_dir = os.path.dirname(os.path.abspath(__file__))
    file_path = os.path.join(current_dir, "prompt.md")
    with open(file_path, "r", encoding="utf-8") as f:
        prompt = f.read()

    messages = [
        {"role": "system", "content": "你是一个JAVA资深测试工程师"},
        {"role": "user", "content":  prompt.replace("{{code}}", content)}
    ]
    client = OpenAI(
        api_key="api-key",
        base_url="https://dashscope.aliyuncs.com/compatible-mode/v1",
    )

    completion = client.chat.completions.create(
        model="qwen-max",
        messages=messages
    )
    code = completion.choices[0].message.content
    start_index = code.find("```java")
    end_index = code.rfind("```")
    if start_index != -1 and end_index != -1:
        code = code[start_index + len("```java"): end_index]
    return code


if __name__ == "__main__":
    target_directory = "D:/company/cloudbank/cloudbank-api"
    scan_java_files(target_directory)