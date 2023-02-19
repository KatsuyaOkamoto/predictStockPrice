package predictStockPricet;
//プロフィールの登録を行うオブジェクトを作る

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class Profile  {

	Connection connect = null;
	Statement statement = null;
	public void profileRegister( ) {

	}

	//MySQLのデータベースにアクセスする。
	//"jdbc:mysql://127.0.0.1:3306", "root", ""
	public void sqlConnect(String hostName, String root, String pass) {
		// TODO 自動生成されたメソッド・スタブ
		try {
			// Class.forName()メソッドにJDBCドライバ名を与えJDBCドライバをロード

			connect = DriverManager.getConnection(hostName, root, pass); //ここで繋げるデータベースを記入する
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("データベースの接続にエラーが発生しました。");
		}
		try {
			System.out.println("データベースに接続できました");
			statement = connect.createStatement();
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Statementクラスの生成に問題が発生しました。");
		}

	}

	//テーブルに新たなレコードを入力
	public void insertRecord(String command) {
		int column;
		// TODO 自動生成されたメソッド・スタブ
		try {
			/*SQL文を実行した結果セットをResultSetオブジェクトに格納している*/
			column = statement.executeUpdate(command);
			System.out.println(column);

		} catch (SQLException e) {
			// TODO: handle exception
			System.out.println("result の値代入にて問題が発生しました。");
			System.out.println(e);
		}
	}

	//テーブルに検索をかける。
	public void sqlQuery(String command) {
		ResultSet result = null;
		// TODO 自動生成されたメソッド・スタブ
		try {
			/*SQL文を実行した結果セットをResultSetオブジェクトに格納している*/
			result = statement.executeQuery(command);

		} catch (SQLException e) {
			// TODO: handle exception
			System.out.println("result の値代入にて問題が発生しました。");
			System.out.println(e);
		}
		try {
			while (result.next()) {
				/*getString()メソッドは、引数に指定されたフィールド名(列)の値をStringとして取得する*/
				ArrayList<Object> array = new ArrayList<Object>();
				for (int i = 1; i <= result.getMetaData().getColumnCount(); i++) { //idProfile,name, number_of_win, number_of_lose
					array.add(result.getString(i));
				}
				System.out.println("以下追加された登録情報");
				System.out.println(array);

			}
			result.close();
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("レコード検索結果に問題が生じました。");
		}
		sqlClose();

	}

	//sqlを終了させる
	public void sqlClose() {
		try {
			statement.close();
			connect.close();
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("データベースを終了できません。");

		}
	}

	//新たなレコードを入力するためのSQL文を作る
	//インフルエンサーを追加
	public String sqlAddStatement(String name) {
		// TODO 自動生成されたメソッド・スタブ
		return "INSERT INTO " + "mydb.Profile" + " ( name ) " + "VALUES" + " ('" + name + "') ";
	}

	//テーブルで検索するためのSQL文を作る
	public String sqlSerchStatement(String column, Object name) {
		// TODO 自動生成されたメソッド・スタブ
		return "SELECT * FROM " + "mydb.Profile"
				+ " WHERE " + column + " = '" + name + "' ";
	}
	//レコードにある一つの銘柄の数を取得
		public String sqlSerchCount(String column, Object name) {
			// TODO 自動生成されたメソッド・スタブ
			return "SELECT COUNT(*) FROM " + "mydb.Profile"
					+ " WHERE " + column + " = " + name + " ";
		}


}
