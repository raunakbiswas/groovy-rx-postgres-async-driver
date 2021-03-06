# groovy-rx-postgres-async-driver
[![Released Version](https://img.shields.io/badge/Version-Released-blue.svg)](https://oss.sonatype.org/content/repositories/snapshots/com/github/leosilvadev/groovy-postgres-async-driver/) [![Build Status](https://travis-ci.org/leosilvadev/groovy-rx-postgres-async-driver.svg?branch=master)](https://travis-ci.org/leosilvadev/groovy-rx-postgres-async-driver) [![Coverage Status](https://coveralls.io/repos/github/leosilvadev/groovy-rx-postgres-async-driver/badge.svg?branch=master)](https://coveralls.io/github/leosilvadev/groovy-rx-postgres-async-driver?branch=master)


Groovy wrapper for <a href="https://github.com/alaisi/postgres-async-driver">postgres-async-driver<a>

###### But why?
- Result mapping based on Templates (Map or Class)
- Execute queries with Named Parameters
- Use Date types as you want: java.util.Date, Calendar, LocalDate, LocalDateTime
- Basic Jsonb native support
- Pagination support

## TODO
- Tests
- Anything you think is useful :)

## Usage

#### Gradle
```groovy
repositories {
    maven {
        url "https://oss.sonatype.org/content/repositories/snapshots"
    }
}

dependencies {
	compile 'com.github.leosilvadev:groovy-rx-postgres-async-driver:0.0.7-SNAPSHOT'
}
```

#### Maven
```xml
<repository>
   <id>oss.sonatype.org.snapshot</id>
   <name>Oss Sonatype Snapshot</name>
   <url>https://oss.sonatype.org/content/repositories/snapshots</url>
</repository>

<dependency>
  <groupId>com.github.leosilvadev</groupId>
  <artifactId>groovy-rx-postgres-async-driver</artifactId>
  <version>0.0.7-SNAPSHOT</version>
</dependency>
```

### Connecting
```groovy
def db = new PgDb([
	hostname: "localhost",
	port: 5432,
	database: "glogger-test",
	username: "dev",
	password: "dev",
	poolSize: 20
])
```

#### All the following methods return an rxjava Observable

## Transactional Methods
**All the non-transactional methods are available on PgTransaction object**

The rollback happens if there is any error inside the Observables, but you can trigger it anytime you want if needed
```groovy
db.transaction { PgTransaction tx ->
	tx.insert(sqlInsert, paramsOne).flatMap({ id ->
		tx.update(sqlUpdate, paramsTwo)
	}).flatMap({ id ->
		tx.delete(sqlDelete, paramsThree)
	}).flatMap({ id ->
		tx.commit()
	})
}.onErrorReturn({
	println 'Transaction was rolled back'
}).subscribe({
	println 'Transaction OK'
})
```

## Non-Transactional Methods

### Insert - Map based
```groovy
def sql = 'INSERT INTO Users (login, password, dateField, timestampField, jsonbField) VALUES (:login, :password, :date, :timestamp, :jsonbField)'
def jsonbObject = [any:'Attrvalue', date:new Date(), sub:[name:'UHA']]
def params = [login:'any', password:'any', date:LocalDate.now(), timestamp:LocalDateTime.now(), jsonbField:jsonbObject]
db.insert(sql, params).subscribe({ id -> println id })
```

### Insert - Class based
```groovy
class User {
	Long id
	String login
	String password
	LocalDate date
	LocalDateTime timestamp
	Map jsonbField
}

def sql = 'INSERT INTO Users (login, password, dateField, timestampField, jsonbField) VALUES (:login, :password, :date, :timestamp, :jsonbField)'
def jsonbObject = [any:'Attrvalue', date:new Date(), sub:[name:'UHA']]
def user = new User(login:'any', password:'any', date:LocalDate.now(), timestamp:LocalDateTime.now(), jsonbField:jsonbObject)
db.insert(sql, user).subscribe({ id -> println id })
```

### Update - Map based
```groovy
def sql = 'UPDATE Users SET password = :password WHERE login = :login'
def params = [login:'mylogin', password:'newpass']
db.update(sql, params).subscribe({ numOfUpdated -> println numOfUpdated })
```

### Update - Class based
```groovy
def sql = 'UPDATE Users SET password = :password WHERE login = :login'
def user = new User(login:'mylogin', password:'newpass')
db.update(sql, user).subscribe({ numOfUpdated -> println numOfUpdated })
```

### Delete
```groovy
def sql = 'DELETE FROM Users WHERE login = :login'
def params = [login:'mylogin']
db.delete(sql, params).subscribe({ numOfDeleted -> println numOfDeleted })
```

### Find - Map based
```groovy
def sql = "SELECT * FROM Users WHERE jsonbField ->> 'any' = 'Attrvalue'"
def template = [id:Long, login:String]
db.find(sql, template).subscribe({ users -> println users })
```

### Find - Class based
```groovy
def sql = "SELECT * FROM Users WHERE jsonbField ->> 'any' = 'Attrvalue'"
db.find(sql, User).subscribe({ users -> println users })
```

### Find - Paging - Map based
```groovy
def sql = 'SELECT * FROM Users WHERE login = :login ORDER BY UserID'
def template = [id:Long, login:String]
def params = [login:'any']
def page = 1
def itemsPerPage = 10
def paging = new PageRequest(page, itemsPerPage)
db.find(sql, template, params, paging).subscribe({ Page page -> println page.items })
```

### Find - Paging - Class based
```groovy
def sql = 'SELECT * FROM Users WHERE login = :login ORDER BY UserID'
def params = [login:'any']
def page = 1
def itemsPerPage = 10
def paging = new PageRequest(page, itemsPerPage)
db.find(sql, User, params, paging).subscribe({ Page page -> println page.items })
```

### Find One - Map based
```groovy
def sql = 'SELECT * FROM Users WHERE id = :id'
def template = [id:Long, login:String]
def params = [id:1]
db.findOne(sql, template, params).subscribe({ user -> println user })
```

### Find One - Class based
```groovy
def sql = 'SELECT * FROM Users WHERE id = :id'
def params = [id:1]
db.findOne(sql, User, params).subscribe({ user -> println user })
```

### Execute
```groovy
def sql = 'DROP TABLE Users'
db.execute(sql).subscribe({ result -> println result })
```
