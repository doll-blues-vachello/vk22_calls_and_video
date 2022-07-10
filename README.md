# VK звонки и видео
Требуется java 11+ (тестировалось на 11)
## Сборка
``./gradlew shadowJar``
## 10 баллов
[Запущенный бот](https://vk.com/public213724548)
### Запуск:
```java -jar build/libs/vk_bot_kt-1.0-SNAPSHOT-all.jar --clientID <client_id> --secret <secret> 10 <group_token>```

Где 
- client_id - id приложения
- secret - secret вашего приложения
- group_token - токен сообщества
### Использование
Отправит в группу сообщение с текстом ``Звонок`` (ркгистр важен)


## 20 баллов
[Запущенный бот](https://vk.com/public214438349)
### Запуск
```java -jar build/libs/vk_bot_kt-1.0-SNAPSHOT-all.jar --clientID <client_id> --secret <secret> 20 <group_token> operator1,operator2,...```


Где
- client_id - id приложения
- secret - secret вашего приложения
- group_token - токен сообщества
- operator1,operator2,... - список id операторов (любое количество)

**ВАЖНО:** Перед запуском бота все операторы должны написать хотя бы одно сообщение в личку сообщества (иначе он не сможет слать им ссобщения и упадёт)

## 30 баллов
```java -jar build/libs/vk_bot_kt-1.0-SNAPSHOT-all.jar --clientID <client_id> --secret <secret> 30 <group_id> <timeout>```

Где
- client_id - id приложения
- secret - secret вашего приложения
- group_id - числовой id группы
- timeout - период обновления в секундах

## 40 баллов
```java -jar build/libs/vk_bot_kt-1.0-SNAPSHOT-all.jar --clientID <client_id> --secret <secret> 40 <video_id> <patterns>```

Где
- client_id - id приложения
- secret - secret вашего приложения
- video_id - id видео (пример: ``-213724548_456239017``)
- patterns - список паттернов

Пример: 
``java -jar build/libs/vk_bot_kt-1.0-SNAPSHOT-all.jar --clientID <client_id> --secret <secrret> 40 -213724548_456239017  '!\s*no' '!\s*yes'``

## 50 баллов
**Для работы прилжения необходим ffmpeg в PATH**

```java -jar build/libs/vk_bot_kt-1.0-SNAPSHOT-all.jar --clientID <client_id> --secret <secret> 50 <group_id> <num_slides> <filename>```


Где
- client_id - id приложения
- secret - secret вашего приложения
- group_id - id группы
- num_slides - Количество слайдов (0 - все)
- filename - имя выходного файла