docker build . -t kunta-api-elasticsearch
docker tag kunta-api-elasticsearch metatavu/kunta-api-elasticsearch
docker push metatavu/kunta-api-elasticsearch