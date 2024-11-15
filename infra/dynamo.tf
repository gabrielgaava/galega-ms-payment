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
}