from pathlib import Path

import requests
import json
import os
import re

class QihooRequest:
    def __init__(self, url, headers):
        self.url = url
        self.headers = headers

    def send_request(self, query):
        data = {
            "inputs": {},
            "query": query,
            "response_mode": "blocking",
            "conversation_id": "",
            "user": "zhangjiaxu"
        }
        
        response = requests.post(self.url, headers=self.headers, data=json.dumps(data))
        print(f"Status Code: {response.status_code}")
        return response.json()["answer"]
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

                test_class_name = Path(test_file_path).name.replace(".java", "")
                matches = re.findall(r"```java(.*?)```", unit_test_content, re.DOTALL)
                code = ""
                for match in reversed(matches):
                    if f" {test_class_name} " in match:
                        code = match.strip()
                if code == "":
                    print(test_file_path)
                    print("生成单元测试失败!")
                    continue
                else:
                    with open(test_file_path, "w", encoding="utf-8") as f:
                        f.write(code)
                    print(test_file_path)
                    print("生成单元测试成功!")


url = 'https://ai-studio-dev.daikuan.qihoo.net/v1/chat-messages'
headers = {
    'Authorization': 'Bearer app-bM0bVrpdfdMuuEtknF2bFrKs',
    'Content-Type': 'application/json'
}
sender = QihooRequest(url, headers)

def generate_test_cases(content):
    query_file_path = 'qihoo.md'
    with open(query_file_path, 'r', encoding='utf-8') as file:
        prompt = file.read()

    prompt = prompt.replace("{{code}}", content)

    try:
        return sender.send_request(prompt)
    except FileNotFoundError as e:
        print(e)
        return None


if __name__ == "__main__":
    target_directory = "D:/company/cloudbank/cloudbank-api"
    scan_java_files(target_directory)