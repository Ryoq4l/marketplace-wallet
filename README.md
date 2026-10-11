# marketplace-wallet

Сервис кошелька для маркетплейса скинов CS. Реализует escrow-модель: деньги покупателя замораживаются на время сделки и списываются только после её подтверждения.

## Назначение

Отвечает за:
- Баланс пользователя с разделением на `available` и `held`
- Пополнение и вывод средств
- Escrow-операции: заморозка (`hold`), разморозка (`release`), возврат (`refund`)
- Историю транзакций
- Регистрацию в Eureka

## Стек

- Java 21, Spring Boot 4.1
- Spring Data JPA, Hibernate
- MySQL 8.4
- Spring Cloud Netflix Eureka Client

## Модель кошелька

Кошелёк хранит два независимых баланса:

- **availableBalance** — доступно пользователю для вывода или новых сделок
- **heldBalance** — заморожено под активные сделки

**totalBalance** = available + held (вычисляется, не хранится).

Операции:
- `deposit` — пополнение available
- `withdraw` — списание из available
- `hold` — перевод из available в held (при создании заказа)
- `release` — списание из held продавцу (при успешной сделке)
- `refund` — возврат из held в available покупателю (при отмене сделки)

## Эндпоинты

| Метод | Путь | Описание |
|---|---|---|
| POST | /api/v1/wallets | Создать кошелёк |
| GET | /api/v1/wallets/user/{userId} | Получить кошелёк по userId |
| POST | /api/v1/wallets/user/{userId}/deposit | Пополнение |
| POST | /api/v1/wallets/user/{userId}/withdraw | Вывод |
| POST | /api/v1/wallets/user/{userId}/hold | Заморозка под сделку |
| POST | /api/v1/wallets/user/{userId}/release | Завершение сделки (в разработке) |
| POST | /api/v1/wallets/user/{userId}/refund | Отмена сделки (в разработке) |

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
| DDL_AUTO | Hibernate ddl-auto | update |

## Запуск локально

Требуется запущенный MySQL с базой `wallet_db`.

\`\`\`bash
mvn spring-boot:run
\`\`\`

## Запуск в Docker

Из корня `marketplace-infra`:

\`\`\`bash
docker compose up --build wallet
\`\`\`

## Транзакции

Каждая операция пишет запись в таблицу `transactions`:

- `type` — DEPOSIT / WITHDRAW / HOLD / RELEASE / REFUND / FEE
- `status` — PENDING / COMPLETED / CANCELLED / FAILED
- `dealId` — ссылка на сделку в order-service (для escrow-операций)
- `amount`, `description`, timestamps

## Статус

В активной разработке. Реализованы: create, get, deposit, withdraw, hold.
В работе: release, refund, защита от гонок, идемпотентность по dealId.
