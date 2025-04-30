import os.path

from transformers import AutoModelForCausalLM, AutoTokenizer


if __name__ == '__main__':
    cache_path = os.path.join(os.path.dirname(__file__), "model_cache")
    print(cache_path)

    model_name = "Qwen/Qwen3-30B-A3B"
    model_path = os.path.join(cache_path, model_name)
    tokenizer = AutoTokenizer.from_pretrained(pretrained_model_name_or_path = model_path, cache_dir=cache_path)
    model = AutoModelForCausalLM.from_pretrained(
        pretrained_model_name_or_path = model_path,
        cache_dir=cache_path,
        torch_dtype="auto",
        device_map="auto"
    )

    prompt = "Give me a short introduction to large language model."
    messages = [
        {"role": "user", "content": prompt}
    ]
    text = tokenizer.apply_chat_template(
        messages,
        tokenize=False,
        add_generation_prompt=True,
        enable_thinking=True # Switches between thinking and non-thinking modes. Default is True.
    )
    model_inputs = tokenizer([text], return_tensors="pt").to(model.device)

    # conduct text completion
    generated_ids = model.generate(
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

    thinking_content = tokenizer.decode(output_ids[:index], skip_special_tokens=True).strip("\n")
    content = tokenizer.decode(output_ids[index:], skip_special_tokens=True).strip("\n")

    print("thinking content:", thinking_content)
    print("content:", content)