# marketplace-wallet

Сервис кошелька для маркетплейса скинов CS. Реализует escrow-модель с разделением баланса на available и held. Деньги покупателя замораживаются при создании сделки и переходят продавцу только после подтверждения трейда и истечения защиты.

## Стек

- Java 21, Spring Boot 4.1
- Spring Data JPA, Hibernate
- MySQL 8.4
- Spring Cloud Netflix Eureka Client
- Spring Kafka (заложен под событийную шину)
- Docker, Docker Compose

## Модель кошелька

Кошелёк хранит два независимых баланса:

- availableBalance — доступно пользователю для вывода или новых сделок
- heldBalance — заморожено под активные сделки

totalBalance = available + held (вычисляется, не хранится).

## Жизненный цикл escrow

    Создание сделки      buyer.available -> buyer.held
    Подтверждение трейда buyer.held      -> seller.held
    Защита истекла       seller.held      -> seller.available
    Отмена до трейда     buyer.held       -> buyer.available

Все переходы — отдельные эндпоинты. Статусом сделки управляет order-service, wallet выполняет операции по запросу.

## Эндпоинты

| Метод | Путь | Описание |
|---|---|---|
| POST | /api/v1/wallets | Создать кошелёк |
| GET | /api/v1/wallets/user/{userId} | Получить кошелёк |
| POST | /api/v1/wallets/user/{userId}/deposit | Пополнение |
| POST | /api/v1/wallets/user/{userId}/withdraw | Вывод |
| POST | /api/v1/wallets/user/{userId}/hold | Заморозка под сделку |
| POST | /api/v1/wallets/user/{buyerId}/release | Перевод продавцу при подтверждении |
| POST | /api/v1/wallets/user/{buyerId}/refund | Возврат при отмене до подтверждения |
| POST | /api/v1/wallets/user/{sellerId}/complete | Финал после истечения защиты |

## Защита от ошибок

Race conditions. Все мутирующие операции используют pessimistic lock (@Lock(PESSIMISTIC_WRITE) + @Query в findByUserIdForUpdate). Два параллельных запроса не смогут прочитать один баланс.

Идемпотентность. Unique index (deal_id, type) в transactions + проверка existsByDealIdAndType до операции. Ретрай от order-service не заморозит дважды.

Pessimistic lock порядок. В операциях с двумя кошельками фиксированный порядок lock'ов — защита от deadlock.

## Переменные окружения

| Переменная | Назначение | Дефолт |
|---|---|---|
| DB_HOST | Хост MySQL | localhost |
| DB_PORT | Порт MySQL | 3306 |
| DB_NAME | Имя базы | wallet_db |
| DB_USER | Пользователь БД | market_user |
| DB_PASSWORD | Пароль БД | market_pass |
| EUREKA_HOST | Хост Eureka | localhost |
| EUREKA_PORT | Порт Eureka | 8761 |
| KAFKA_HOST | Хост Kafka | localhost |
| KAFKA_PORT | Порт Kafka | 9092 |
| DDL_AUTO | Hibernate ddl-auto | update |

## Запуск

Локально из IDE. Требуется MySQL с базой wallet_db на localhost:3306.

    mvn spring-boot:run

В Docker. Из корня marketplace-infra:

    docker compose up --build wallet

Wallet стартует после mysql (healthcheck) и eureka, регистрируется в реестре как MARKETPLACE-WALLET на порту 6767.

## Модель данных

wallets
- id (UUID), user_id (UUID, unique)
- available_balance, held_balance (BigDecimal)
- currency (USD/EUR/RUB/BYN), status (ACTIVE/BLOCKED/CLOSED)
- created_at, updated_at

transactions
- id (UUID), wallet_id (FK)
- type (DEPOSIT/WITHDRAW/HOLD/RELEASE/REFUND/FEE/COMPLETE)
- amount (BigDecimal), status (PENDING/COMPLETED/FAILED/CANCELLED/TIMEOUT)
- deal_id (UUID, nullable), description
- created_at, updated_at
- unique index: (deal_id, type) — защита от двойных операций по одной сделке

## Статус

Основные escrow-операции реализованы: hold, release, refund, complete. Идемпотентность и защита от гонок на балансе — на месте.

В планах: disputeRefund (реверс после подтверждения), интеграция с order-service через Feign, публикация событий в Kafka.
