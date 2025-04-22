# n8n

## docker deploy
设置语言为中文: -e N8N_DEFAULT_LOCALE=zh-CN 
```
docker volume create n8n_data
docker run -it --name n8n -e N8N_DEFAULT_LOCALE=zh-CN -e LANG=C.UTF-8 -p 5678:5678 -v D:/docker/Volumes/n8n:/home/node/.n8n docker.n8n.io/n8nio/n8n
```
[本地连接](http://localhost:5678)