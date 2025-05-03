import torch


if __name__ == '__main__':
    print("GPU 开启:" + str(torch.cuda.is_available()))
    while True:
        question = input('What is your question?\r\n: ')
        print(question)
        if question == 'quit':
            break