Многомодульный проект бэкенда приложения афишы событий, состоящий из двух сервисов:
- Сервис статистики, работающий через http клиента, отправляющего запросы на сервер-статистики, 
где реализована логика работы и хранения данных в бд.
- Основной сервис,который включает в себя три уровня API: административный, для зарегистрированных 
пользователей и публичный. Основные сущности это юзер, событие, заявки на участие в событии, категории
событий и подборки. Логика сервиса реализовывает основные CRUD операции, а также сделана тестовая версия
хэширования данных сервиса статистики.
