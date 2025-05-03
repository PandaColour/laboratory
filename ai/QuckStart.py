import os.path

from transformers import AutoModelForCausalLM, AutoTokenizer


class BigModel:
    def __init__(self):
        self.model = None
        self.tokenizer = None
    def load_model(self, model_name):
        cache_path = os.path.join(os.path.dirname(__file__), "model_cache")
        model_path = os.path.join(cache_path, model_name)
        self.tokenizer = AutoTokenizer.from_pretrained(pretrained_model_name_or_path=model_path, cache_dir=cache_path)
        self.model = AutoModelForCausalLM.from_pretrained(
            pretrained_model_name_or_path = model_path,
            cache_dir=cache_path,
            torch_dtype="auto",
            device_map="auto"
        )

    def generate_text(self, enable_thinking: bool = True, messages: list = []):
        text = self.tokenizer.apply_chat_template(
            messages,
            tokenize=False,
            add_generation_prompt=True,
            enable_thinking=enable_thinking
        )
        model_inputs = self.tokenizer([text], return_tensors="pt").to(self.model.device)

        # conduct text completion
        generated_ids = self.model.generate(
            **model_inputs,
            max_new_tokens=32768
        )
        output_ids = generated_ids[0][len(model_inputs.input_ids[0]):].tolist()

        # parsing thinking content
        try:
            # rindex finding 151668 (</think>)
            index = len(output_ids) - output_ids[::-1].index(151668)
        except ValueError:
            index = 0

        thinking_content = self.tokenizer.decode(output_ids[:index], skip_special_tokens=True).strip("\n")
        content = self.tokenizer.decode(output_ids[index:], skip_special_tokens=True).strip("\n")
        return thinking_content, content

if __name__ == '__main__':
    bigModel = BigModel()

    model_name = "microsoft/Phi-4-mini-reasoning"
    bigModel.load_model(model_name)

    while True:
        question = input("You:\r\n")
        with open("./prompt.md", "r") as f:
            prompt = f.read()

        messages = [
            {"role": "system", "content": prompt},
            {"role": "user", "content": question},
        ]
        thinking_content, content = bigModel.generate_text(messages=messages)

        print("thinking content:\r\n", thinking_content)
        print("content:\r\n", content)