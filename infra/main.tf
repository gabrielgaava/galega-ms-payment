provider "aws" {
  region = "us-east-1"
}

resource "aws_dynamodb_table" "payment" {
  name           = "Payment"
  billing_mode   = "PROVISIONED"  # Modo provisionado (alternativa: PAY_PER_REQUEST)
  read_capacity  = 5
  write_capacity = 5

  hash_key       = "id"
  range_key      = "externalId"

  attribute {
    name = "id"
    type = "S"
  }

  attribute {
    name = "externalId"
    type = "S"
  }

  # Definindo o índice global secundário (GSI) no campo externalId
  global_secondary_index {
    name               = "externalId-index"
    hash_key           = "externalId"    # Usando 'externalId' como chave de hash do GSI
    projection_type    = "ALL"            # Projeção de todos os atributos (pode ser "KEYS_ONLY" ou "INCLUDE")
    read_capacity      = 5
    write_capacity     = 5
  }
}

// Repositorio da Imagem Docker do app
resource "aws_ecr_repository" "galega-ms-payment-app" {
  name                 = "galega-ms-payment-app"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = {
    Project     = "galega"
  }
}