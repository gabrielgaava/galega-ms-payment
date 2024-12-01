# Microserviço de Pagamento - Galega Burger
![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![AWS](https://img.shields.io/badge/AWS-%23FF9900.svg?style=for-the-badge&logo=amazon-aws&logoColor=white)
![AmazonDynamoDB](https://img.shields.io/badge/Amazon%20DynamoDB-4053D6?style=for-the-badge&logo=Amazon%20DynamoDB&logoColor=white)
![Kubernetes](https://img.shields.io/badge/kubernetes-%23326ce5.svg?style=for-the-badge&logo=kubernetes&logoColor=white)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=gabrielgaava_galega-ms-payment&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=gabrielgaava_galega-ms-payment)

Este repositorio tem como função agregar todo o escopo de pagamento dos pedidos dentro 
da solução propsota no Tech Challenge da Fiap para o Pos-Tech de Egenharia de Software.

## Estrutura
O projeto foi construindo com a arquitetura Hexagonal e visando integração com cluster EKS (Kubernetes)

## Rodando Localmente
### 1. Para Iniciar o DynamoDB com Docker
docker run -p 8000:8000 amazon/dynamodb-local -jar DynamoDBLocal.jar -inMemory -sharedDb

### 2. Para criar tabela no DynamoDB  
aws dynamodb create-table --cli-input-json file://createPaymentTable.json --endpoint-url http://localhost:8000
