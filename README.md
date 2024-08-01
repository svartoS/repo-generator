# GitHub to Bitbucket Repository Migrator
Этот инструмент автоматизирует перенос репозиториев Git из GitHub в Bitbucket, избавляя от необходимости переносить каждый репозиторий и его ветки вручную.
### Проблема
Перенос большого количества репозиториев Git из GitHub в Bitbucket вручную может быть утомительным и трудоемким процессом, особенно если учесть возможное количество репозиториев (более 1000) и наличие нескольких веток в каждом.
### Решение
Этот инструмент автоматизирует процесс переноса репозиториев, обеспечивая быстрое и эффективное решение для миграции данных.
## Возможности
Массовый перенос: Перенос всех репозиториев из указанной учетной записи GitHub в Bitbucket.  
Перенос веток: Перенос всех веток для каждого репозитория с сохранением истории коммитов.  
Гибкость: Возможность переноса только выбранных репозиториев или веток.  
Прогресс и логирование: Отображение прогресса миграции и ведение логов для отслеживания успешных переносов и ошибок.  
![image](https://github.com/user-attachments/assets/f16c5648-6c4f-4e33-988c-fd747c287523)

## Требования
Java Development Kit (JDK) 8 или выше.
Apache Maven 3.x.
## Инструкции по установке
Клонируйте этот репозиторий: git clone https://github.com/svartoS/repo-generator.git  
Перейдите в каталог проекта: cd repo-generator  
Соберите проект: mvn clean install  
Перейдите на http://localhost:8080/  
## Инструкции по использованию
Отредактируйте application.yml, указав ваши учетные данные GitHub и Bitbucket (токены API).  
Запустите приложение: java -jar target/repo-generator.jar  
Пример файла application.yml  
```
github:  
  username: username  
  accessToken: ghp_accesstoken  
  api-url: https://api.github.com  
  
bitbucket:  
  username: username  
  owner-name: usernameOwner  
  password: ATBbitbuckettoken  
  api-url: https://api.bitbucket.org/2.0
``` 
## Если у вас есть вопросы или предложения, не стесняйтесь обращаться!
tg @svartosan
