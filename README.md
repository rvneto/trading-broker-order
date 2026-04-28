# 🚀 Broker Order API

Microserviço responsável pela orquestração, validação e submissão de ordens de compra e venda de ativos para o ecossistema do Broker e integração com o simulador da B3.

## 🛠️ Tecnologias Utilizadas
* **Java 21** & **Spring Boot 3.3.5**
* **Spring Cloud OpenFeign**: Comunicação síncrona com a Wallet API para validação de saldo.
* **Apache Kafka**: Mensageria interna para eventos de auditoria e status (tópico `order-events-v1`).
* **RabbitMQ**: Integração externa (AMQP) para envio de ordens ao Matching Engine (fila `mq-broker-to-b3`).
* **MySQL 8.0**: Persistência do histórico de ordens (Banco: `broker_order_db`).
* **Flyway**: Gestão de migrations e versionamento do banco.
* **Lombok & MapStruct**: Produtividade e mapeamento de DTOs.

## 📋 Pré-requisitos (Infraestrutura)
Certifique-se de que os seguintes serviços estão rodando no seu Docker:
* **MySQL**: Disponível na porta `3308`.
* **Kafka**: Disponível na porta `9092`.
* **RabbitMQ**: Disponível na porta `5672` (Management na `15672`).
* **Wallet API**: Deve estar ativa na porta `8081` para as chamadas de validação.

## 🔧 Configuração e Execução
1. Configure as credenciais de banco e endereços de mensageria no `src/main/resources/application.yaml`.
2. Compile e execute o projeto:
   ```bash
   mvn clean install
   mvn spring-boot:run
3. A API estará disponível em: http://localhost:8082

## 📖 Documentação da API (Swagger)
Acesse a interface interativa para testes em: ```http://localhost:8082/swagger-ui.html```

Exemplo de Payload (POST /api/v1/orders)

```json
{
  "userId": "e82b8e13-1df9-41ba-a961-a28a4fe4e38b",
  "ticker": "PETR4",
  "quantity": 10,
  "price": 35.50,
  "side": "BUY"
}
```
## 🔄 Fluxograma da Ordem
1. Request: O Controller recebe a ordem e valida os campos obrigatórios.
2. Saldo: O Service chama a broker-wallet-api via Feign para checar o availableBalance.
3. Persistência: Se houver saldo, a ordem é salva no MySQL com status OPEN.
4. Internal Event: Publica no Kafka (usado para atualizar posições e histórico).
5. External Command: Envia para a fila RabbitMQ da B3 para execução no pregão simulado.