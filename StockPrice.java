package predictStockPrice;
//ネットから取得したデータをsql(StockPrice)に挿入する

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class StockPrice  {
	Connection connect = null;
	Statement statement = null;
	int code;
	ArrayList<Object> codeUpdateList = new ArrayList<Object>();	//mydb.Predictとmydb.StockPriceを比較するときのコード

	//銘柄をデータ（`StockPrice`）に登録,下のinsertRecordに挿入する
	public boolean registerStock(int code) throws IOException, SQLException {
		// TODO 自動生成されたメソッド・スタブ
		ResultSet result = null;
		sqlConnect("jdbc:mysql://127.0.0.1:3306", "root", "");
		System.out.println("データ（`StockPrice`）に銘柄が存在しているか検索します。以下、結果");
		result = statement.executeQuery(sqlSerchCount("code", code)); //'mydb.StockPrice'にコードが存在するか検索 結果は個数
		result.next();
		System.out.println(result.getString(1));	//個数を表示
		if(result.getInt(1) >= 1) {
			System.out.println("データベースに既に存在してます。終了します。");
			return true;

		}else {
			System.out.println("結果は0個です。株データをデータベースに保存します。");
		}
		StockSerch stockSerch = new StockSerch();
		//株価などをインターネットから取得し各値をセット
		try {
			stockSerch.reserchStock(code);
		}catch (Exception e) {
			// TODO: handle exception
			System.out.println("ネットでの取得に問題が発生しました。falseにします。");
			return false;
		}

		//データ（`StockPrice`）に挿入
		try {
			insertRecord(sqlAddStatement(code, stockSerch.getBrand(), stockSerch.getOpeningPrice(),
					stockSerch.getClosingPrice(), stockSerch.getHighPrice(), stockSerch.getHighPriceTime(),
					stockSerch.getLowPrice(), stockSerch.getLowPriceTime(), stockSerch.getUpdateDay()));
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("データ挿入に失敗しました。falseにします。");
			return false;
		}

		//データを検索
		System.out.println("銘柄を追加しました。以下株価情報");
		sqlQuery(sqlSerchStatement("code", code));
		sqlClose();
		return true;

	}

	//データ（`StockPrice`）にある株価を更新する。
	public void updateStockPrice(int code) throws SQLException {
		// TODO 自動生成されたメソッド・スタブ
		ResultSet result = null;
		sqlConnect("jdbc:mysql://127.0.0.1:3306", "root", "");
		System.out.println("以下、結果");
		result = statement.executeQuery(sqlSerchCount("code", code));	//'mydb.StockPrice'にコードが存在するか検索　結果は個数
		result.next();
		System.out.println(result.getString(1));	//個数を表示
		if(result.getInt(1) != 1) {
			System.out.println("データベースにコードが存在しない、またはコードが重複しています。終了します。");
			System.exit(0);

		}else {
			System.out.println("結果は一個です。");
		}
		StockSerch stockSerch = new StockSerch();
		try {
			stockSerch.updateReserchStock(code);
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			System.out.println("コードの情報を得るメソッド失敗");
		}
		System.out.println("データを再挿入します。");
		//データを再挿入
		int sounyu = statement.executeUpdate(sqlUpdateStatement(code, stockSerch.getOpeningPrice(),
				stockSerch.getClosingPrice(), stockSerch.getHighPrice(), stockSerch.getHighPriceTime(),
				stockSerch.getLowPrice(), stockSerch.getLowPriceTime(), stockSerch.getUpdateDay()));
		//データを検索
				System.out.println("銘柄の内容を修正しました。以下株価情報");
				sqlQuery(sqlSerchStatement("code", code));
				sqlClose();
	}


	//更新するための株価データの銘柄コード'code'抜き取り
	public void getcode() {
		ResultSet result = null;
		// TODO 自動生成されたメソッド・スタブ
		try {
			/*SQL文を実行した結果セットをResultSetオブジェクトに格納している*/
			//mydb.Predictにある'flag(1or0)'が1である'code'をグループで抜き取る
			result = statement.executeQuery("SELECT StockPrice_code FROM " + "mydb.Predict"
					+ "WHERE flag(1or0) = 1"
					+ "GROUP BY StockPrice_code");

		} catch (SQLException e) {
			// TODO: handle exception
			System.out.println("result の値代入にて問題が発生しました。");
			System.out.println(e);
		}
		try {
			while (result.next()) {
				/*ResultSetオブジェクトの現在行にある指定された列の値をintとして取得します。*/
					codeUpdateList.add(result.getInt(1));

				System.out.println(codeUpdateList);

			}
			result.close();
		} catch (Exception e) {
			System.out.println("レコード検索結果に問題が生じました。");
		}
	}

	//MySQLのデータベースにアクセスする。
	public void sqlConnect(String hostName, String root, String pass) {
		// TODO 自動生成されたメソッド・スタブ
		try {
			// Class.forName()メソッドにJDBCドライバ名を与えJDBCドライバをロード

			connect = DriverManager.getConnection(hostName, root, pass); //ここで繋げるデータベースを記入する
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("データベースの接続にエラーが発生しました。");
			System.out.println(e);
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
	public void insertRecord(String command) throws SQLException {
		// TODO 自動生成されたメソッド・スタブ
		int column;

			column = statement.executeUpdate(command);
			System.out.println("追加したカラムの数：" + column);
	}

	//新たなレコードを入力するためのSQL文を作る
	//株（銘柄）追加  table = "mydb.StockPrice"
	public String sqlAddStatement(int code, String brand, double openingPrice,
			double closingPrice, double highPrice, String highPriceTime, double lowPrice, String lowPriceTime,
			String updateDay) {
		// TODO 自動生成されたメソッド・スタブ
		return "INSERT INTO " + "mydb.StockPrice " + "VALUES" + " (" + code + ", " + brand + ", " + openingPrice + ", "
				+ closingPrice + ", " + highPrice
				+ ", " + highPriceTime + ", " + lowPrice + ", " + lowPriceTime + ", " + updateDay + ") ";
	}

	//テーブルで検索するためのSQL文を作る
	public String sqlSerchStatement(String column, Object name) {
		// TODO 自動生成されたメソッド・スタブ
		return "SELECT * FROM " + "mydb.StockPrice"
				+ " WHERE " + column + " = " + name + " ";
	}
	//レコードにある一つの銘柄の数を取得
	public String sqlSerchCount(String column, Object name) {
		// TODO 自動生成されたメソッド・スタブ
		return "SELECT COUNT(code) FROM " + "mydb.StockPrice"
				+ " WHERE " + column + " = " + name + " ";
	}

	//データ（StockPrice）にあるコードからレコードを更新するSQL文を作る。
	public String sqlUpdateStatement(int code, double openingPrice,
			double closingPrice, double highPrice, String highPriceTime, double lowPrice, String lowPriceTime,
			String updateDay) {
		return "UPDATE mydb.StockPrice"
				+ " SET " + "openingPrice = " + openingPrice + ", closingPrice = " + closingPrice + ", highPrice = "
				+ highPrice
				+ ", highPriceTime = " + highPriceTime + ", lowPrice = " + lowPrice + ", lowPriceTime = " + lowPriceTime
				+ ", updateDay = " + updateDay
				+ " WHERE  code = " + code;

	}

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
				System.out.println(array);
			}
			result.close();
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("レコード検索結果に問題が生じました。");
		}

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


}
