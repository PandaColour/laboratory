# train_finetune.py
from transformers import (
    AutoModelForCausalLM,
    AutoTokenizer,
    TrainingArguments,
    Trainer,
    DataCollatorForLanguageModeling
)
from datasets import Dataset
import torch
import json

# 加载基础模型（假设已下载DeepSeek-R1-7B模型文件）
model_path = "deepseek-r1-7b"
tokenizer = AutoTokenizer.from_pretrained(model_path)
model = AutoModelForCausalLM.from_pretrained(
    model_path,
    torch_dtype=torch.bfloat16,
    device_map="auto"
)

# 加载会议数据（假设conference空间内容为JSON格式）
with open("conference_data.json") as f:
    conf_data = json.load(f)

# 转换为对话格式
def format_data(example):
    return {
        "text": f"<|system|>\n{example['system']}\n<|user|>\n{example['question']}\n<|assistant|>\n{example['answer']}"
    }

dataset = Dataset.from_list(conf_data).map(format_data)

# 配置LoRA参数
from peft import LoraConfig, get_peft_model

lora_config = LoraConfig(
    r=8,
    lora_alpha=32,
    target_modules=["q_proj", "v_proj"],
    lora_dropout=0.05,
    bias="none",
    task_type="CAUSAL_LM"
)
model = get_peft_model(model, lora_config)

# 设置训练参数
training_args = TrainingArguments(
    output_dir="./output",
    per_device_train_batch_size=2,
    gradient_accumulation_steps=4,
    learning_rate=2e-5,
    num_train_epochs=3,
    fp16=True,
    save_strategy="epoch",
    logging_steps=50,
    report_to="none"
)

# 创建训练器
trainer = Trainer(
    model=model,
    args=training_args,
    train_dataset=dataset,
    data_collator=DataCollatorForLanguageModeling(tokenizer, mlm=False)
)

# 执行训练
trainer.train()

# 合并权重并保存
merged_model = model.merge_and_unload()
merged_model.save_pretrained("./fine_tuned_model")
tokenizer.save_pretrained("./fine_tuned_model")