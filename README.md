Многомодульный проект бэкенда приложения афишы событий, состоящий из двух сервисов:
- Сервис статистики, работающий через http клиента, отправляющего запросы на сервер-статистики, 
где реализована логика работы и хранения данных в бд.
- Основной сервис,который включает в себя три уровня API: административный, для зарегистрированных 
пользователей и публичный. Основные сущности это юзер, событие, заявки на участие в событии, категории
событий и подборки. Логика сервиса реализовывает основные CRUD операции, а также сделана тестовая версия
хэширования данных сервиса статистики.

Описание фичи:
- Реализация хэширования данных статистики для того, что бы при вызове публичных эндпоинтов получения событий, статистика
бралась из хэшмапы, в которой хранится количество просмотров для каждого события по его айди, а при отсутствии данных
происходил бы вызов с базы данных всех событий, которые отсутствуют в мапе. Информация в хэш-таблице обновляется каждые
30 сек., делая запрос на количество просмотров по всем опубликованным событиям.
- В папке постман лежит два теста на проверку реализации хэширования, 
в прескриптах прописана логика порядка вызовов для тестирования.
