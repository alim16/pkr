// package mo.jdbc

// import java.sql.DriverManager
// import java.sql.Connection
// import java.util.Properties

// import cats.effect._
// //import org.postgresql.core.ConnectionFactory //not used

// object doStuff {

//     //connection to local postgress
//     val driver = "org.postgresql.Driver"
//     val url = "jdbc:postgresql://localhost:5432/postgres"
//     val username = "mahamed"
//     val password = "mohamed1"

//     val url2:String = "jdbc:postgresql://localhost:5432/postgres?user=mahamed&password=admin&ssl=true";
//     ///this password "admin" works

//     //is this necessary
//     var connection: Connection = null

//     def apply():IO[Unit] = {

//         try {
//             //make connection
//             Class.forName(driver)
//             connection = DriverManager.getConnection(url2)
//             //connection = DriverManager.getConnection(url,username,password)

//             //create statement and run select query
//             val statement = connection.createStatement()
//             val resultSet = statement.executeQuery("SELECT host, user From users")

//             while (resultSet.next()){
//                 val host = resultSet.getString("host")
//                 val user = resultSet.getString("user")
//                 println("host, user = " + host + ", " + user)
//             }

//         }catch {
//             case e : Throwable => e.printStackTrace()
//         }
//         //doesn't do anything yet, just checking for compilation errors
//         IO.pure(println("###finished db query"))
//     }
// }