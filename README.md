# kunta-api-server
Kunta API server

[![Coverage Status](https://coveralls.io/repos/github/Metatavu/kunta-api-server/badge.svg?branch=develop)](https://coveralls.io/github/Metatavu/kunta-api-server?branch=develop)

## Docker

### Using kautil from Docker

Docker image contains kautil.py Python script. Script contains various utility methods for using Kunta API for development purposes. 

#### Resolve organization id by business name and business code
    
You can resolve organization's Kunta API id by running following command against running container (requires readonly client):
    
    docker exec $(docker ps -q --filter=name=kunta-api-server_kuntaapi_ | head -n 1) /opt/docker/kautil.py --command=get-organization-id --client-id=[CLIENT_ID] --client-secret=[CLIENT_SECRET] --business-name=[BUSINESS_NAME] --business-code=[BUSINESS_CODE]. 

#### Retrieve and update organization settings
    
You can retrieve organization's settings with following command (requires unrestricted client):
    
    docker exec $(docker ps -q --filter=name=kunta-api-server_kuntaapi_ | head -n 1) /opt/docker/kautil.py --command=get-organization-setting --client-id=[CLIENT_ID] --client-secret=[CLIENT_SECRET] --organization-id=[KUNTA_API_ORGANIZATION_ID] --name=[SETTING_KEY]
    
... and change organization's settings with following command (requires unrestricted client):
    
    docker exec $(docker ps -q --filter=name=kunta-api-server_kuntaapi_ | head -n 1) /opt/docker/kautil.py --command=set-organization-setting --client-id=[CLIENT_ID] --client-secret=[CLIENT_SECRET] --organization-id=[KUNTA_API_ORGANIZATION_ID] --name=[SETTING_KEY] --value=[NEW_VALUE]