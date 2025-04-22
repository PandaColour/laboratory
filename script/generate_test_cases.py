import os
from pathlib import Path
from openai import OpenAI

def scan_java_files(model_path):
    model_path = model_path + "/src/main"
    for root, dirs, files in os.walk(model_path):
        for file in files:
            if file.endswith(".java"):
                file_path = os.path.join(root, file)
                with open(file_path, "r", encoding="utf-8") as f:
                    content = f.read()

                test_file_path = file_path.replace("/main", "/test")
                test_file_path = test_file_path.replace(".java", "Test.java")

                if os.path.exists(test_file_path):
                    continue

                if "public interface" in content:
                    continue

                if "public @interface" in content:
                    continue

                Path(test_file_path).parent.mkdir(parents=True, exist_ok=True)
                unit_test_content = generate_test_cases(content=content)
                with open(test_file_path, "w", encoding="utf-8") as f:
                    f.write(unit_test_content)

                print(test_file_path)
                print("生成单元测试成功!")

def generate_test_cases(content):
    messages = [
        {"role": "system", "content": "你是一个专职单元测试的程序员"},
        {"role": "user", "content":  "基于java8的JUnit4, Mockito, Mockito-online,不要尝试使用PowerMockit,JMockit生成单元测试,直接输出代码,不需要其他任何提示下面是代码: \r\n" + content}
    ]
    client = OpenAI(
        # 从环境变量中读取您的方舟API Key
        api_key="b1923890-9d2e-46cd-b238-c8f7e9486a6f",
        base_url="https://ark.cn-beijing.volces.com/api/v3",
    )

    completion = client.chat.completions.create(
        model="deepseek-v3-250324",
        messages=messages
    )
    code = completion.choices[0].message.content
    start_index = code.find("```java")
    end_index = code.rfind("```")
    if start_index != -1 and end_index != -1:
        code = code[start_index + len("```java"): end_index]
    return code


if __name__ == "__main__":
    target_directory = "D:/company/cloudbank/cloudbank-ags"
    scan_java_files(target_directory)