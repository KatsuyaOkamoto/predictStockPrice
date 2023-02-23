package predictStockPrice;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

//クローリングする時、既に実行していたかどうかを確認する（してた場合はプログラム停止）
//クローリングし終わったらデータmydb.TimeStampTableにあるlastUpdateを今日の日付で更新する。
public class TimeStamp {
	Connection connect = null;
	Statement statement = null;
	String todayStr ; //本日の日付（@@@@(年)-@@(月)-@@(日)）
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	TimeStamp(){
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		todayStr = sdf.format(timestamp);
		sqlConnect("jdbc:mysql://127.0.0.1:3306", "root", "");
	}
	//mydb.TimeStampTableにあるlastUpdateが本日の日付でないか確認する。本日の日付の場合trueとしてその実行を停止する。
	public boolean updateCheck() throws SQLException {
		ResultSet result = statement.executeQuery("SELECT * FROM " + "mydb.TimeStampTable");
		result.next();
		try {
			if(todayStr.equals(result.getString(1))) {
				System.out.println("lastUpdateは本日の日付と一致してます。プログラムを終了させます。");
				return true;	//本日の日付と一致。 プログラムを終了させる。
			}
			System.out.println("lastUpdateは本日の日付と一致していません。プログラムを続行させます。");
			return false;	//本日の日付と不一致。 クローリングを実行させる。
		}finally {
			result.close();
		}


	}
	//クローリング終了でデータmydb.PredictにあるlastUpdateを今日の日付で更新する。
	public void writeUpdate() throws SQLException{
		int writeTimeStamp =  statement.executeUpdate("UPDATE mydb.TimeStampTable SET lastUpdate = '" + todayStr + "'");
		System.out.println("writeUpdateメソッド終了");
		sqlClose();
	}

	//今日の日付とクローリングするmydb.PredictのcreatedDayが2日以上の差があるかチェックする。
	public boolean createGap(String createDay) {
		try {
			Calendar cal1 = Calendar.getInstance();
		    Calendar cal2 = Calendar.getInstance();
		    cal1.setTime(sdf.parse(todayStr));
		    cal2.setTime(sdf.parse(createDay));
		    cal2.add(Calendar.DATE, 1);
		    return cal1.after(cal2);
		} catch (Exception e) {
			return false;
		}
	}

	//MySQLのデータベースにアクセスする。
		public void sqlConnect(String hostName, String root, String pass) {
			// TODO 自動生成されたメソッド・スタブ
			try {
				connect = DriverManager.getConnection(hostName, root, pass); //ここで繋げるデータベースを記入する
			} catch (Exception e) {
				// TODO: handle exception
				System.out.println("データベースの接続にエラーが発生しました。");
				System.exit(0);
			}
			try {
				System.out.println("データベースに接続できました");
				statement = connect.createStatement();
			} catch (Exception e) {
				// TODO: handle exception
				System.out.println("Statementクラスの生成に問題が発生しました。");
				System.exit(0);
			}
		}
	//sqlを終了させる。
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
