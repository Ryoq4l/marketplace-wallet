# marketplace-wallet

Сервис кошелька для маркетплейса скинов CS. Реализует escrow-модель с разделением баланса на `available` и `held`. Деньги покупателя замораживаются при создании сделки и переходят продавцу только после подтверждения трейда и истечения защиты.

## Стек

- Java 21, Spring Boot 4.1
- Spring Data JPA, Hibernate
- MySQL 8.4
- Spring Cloud Netflix Eureka Client
- Spring Kafka (заложен под событийную шину)
- Docker, Docker Compose

## Модель кошелька

Кошелёк хранит два независимых баланса:

- **availableBalance** — доступно пользователю для вывода или новых сделок
- **heldBalance** — заморожено под активные сделки

`totalBalance` = available + held (вычисляется, не хранится).

## Жизненный цикл escrow
