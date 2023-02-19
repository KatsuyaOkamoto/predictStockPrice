package predictStockPrice;
//プロフィール（mydb.Profile）にある名前を使い「mydb.Predict」で株価の予想を立てる。
//実行はguessInputメソッド

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.InputMismatchException;
import java.util.Scanner;

public class GuessSystem {
	Connection connect = null;
	Statement statement = null;
	private String Profile_name;	//予測する名前
	private int Profile_idProfile;	//予測する名前のID
	private String StockPrice_brand;	//銘柄
	private int StockPrice_code;	//銘柄のコード
	private double takeProfit;	//利確する値
	private double stopLoss;	//損切りする値
	private String comment;	//登録するにあたってコメント
	double nowPrice;	//銘柄の現在の株価
	private String createdDay; //更新日
	static SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");

	//mydb.Profileに名前が存在しているか確認する。
	public boolean profileCheck(String name) {
		System.out.println(name + "が１つだけ存在するかmydb.Profileに確認します。");
		Profile inagoProfile = new Profile();
		inagoProfile.sqlConnect("jdbc:mysql://127.0.0.1:3306", "root", "");
		ResultSet result = null;
		try {
			result = inagoProfile.statement.executeQuery(inagoProfile.sqlSerchCount("name", "'" + name + "'"));
			result.next();
//			System.out.println("検索結果は" + result.getString(1) + "つ存在していました。");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		int id = 0;
		int x = 0;
		try {
			x = result.getInt(1);	//データベースにある名前の個数の取得（１つだったらよし）
			if (x != 1) {	//名前が重複してる場合
				System.out.println(name + "データベースに存在しない、または" + name + "が重複しています。終了します。");
				return false;

			} else {
				System.out.println("結果は一個です。");
				result = inagoProfile.statement.executeQuery(inagoProfile.sqlSerchStatement("name", name));
				result.next();
				id = result.getInt(1);
				System.out.println(name + "のid番号は" + id + "です。");
				setProfile_name(name);	//nameをセット。SQLに挿入する用
				setProfile_idProfile(id);	//idをセット。SQLに挿入する用
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("判定されませんでした。結果をfalseにします。");
		return false;

	}

	//クラスStockPriceを利用し株の銘柄が存在するか確認する。無かったら銘柄を追加しておく。
	public boolean stockCodeCheck(int code) throws IOException, SQLException {
		StockPrice stockPrice = new StockPrice();
		try {
			if (stockPrice.registerStock(code)) { //StockPriceのregisterStockメソッドがtrueならtrueにする。銘柄がデータに存在しない場合でもメソッドで追加してtrue判定にする。
				return true;
			} else {
				System.out.println("クラスStockPriceのメソッドregisterSockの判定はfalseでした。");
				return false;
			}
		} finally {
			try {
				ResultSet brandResult = null;
				brandResult = stockPrice.statement.executeQuery(stockPrice.sqlSerchStatement("code", code));	//銘柄コード（code）を入力して銘柄名（brand）を出力する。
				brandResult.next();
				setStockPrice_brand(brandResult.getString("brand"));
				setStockPrice_code(code);
				nowPrice = brandResult.getDouble("closingPrice");
			} catch (Exception e) {
				System.out.println("セットできるコードがない、またはデータ検索でエラーが発生しました。プログラムを終了させます。");
				System.exit(0);
			}

		}

	}

	//名前と銘柄の株価の予想を入力し、データに出力する。
	public void guessInput(String prfile_name, int code) throws IOException, SQLException {

		//profileCheckメソッドとstockCodeCheckメソッドを使い問題がないか判断して、mydb.Predictデータに挿入する。
		if (!(profileCheck(prfile_name) && stockCodeCheck(code))) {
			System.out.println("profileCheckメソッドまたはstockCodeCheckメソッドにてエラーが発生しました。終了します。");
			return;
		}
		//mydb.Predictデータ内にprfile_nameとcodeの組み合わせが既に入っている場合、再度挿入するか確認する。
		Scanner sc = new Scanner(System.in);
		sqlConnect("jdbc:mysql://127.0.0.1:3306", "root", "");
		int num;
		int select = 0;
		num = sqlGuessCount();
		StockPrice newStock = new StockPrice();
		newStock.updateStockPrice(code);
		if (num > 0) {
			System.out.println(prfile_name + " , " + code + "の組み合わせは既に" + num + "組存在しています。");
			System.out.println("新たに予想されますか？ はい: 1 , いいえ（やり直す）: 2");
			do {
				try {
					select = sc.nextInt();
				} catch (InputMismatchException e) {
					System.out.println("数字意外を打ったからエラーだよね！");
					System.out.println(e);
					System.out.println("数字を打ってください。");
					sc.next();
				}

			} while (!(select == 1 || select == 2));
			switch (select) {
			case 1: //プログラム続行
				System.out.println("「はい」が選択されました。");
				break;
			case 2: //プログラム終了
				System.out.println("「いいえ」が選択されました。プログラムを閉じます。");
				System.exit(0);
			}
		}
		System.out.println("idProfile = " + getProfile_idProfile() + ", brand = " + getStockPrice_brand());

		do {
			System.out.println("利確値と損切り値を入力してください。現在値:" + nowPrice);
			System.out.print("利確値:");
			setTakeProfit(scanInt());
			while(getTakeProfit() <= nowPrice) {
				System.out.println("現在値よりも高い値を入力してください。");
				System.out.print("利確値:");
				setTakeProfit(scanInt());
			}
			System.out.print("損切り値:");
			setStopLoss(scanInt());
			while(getStopLoss() >= nowPrice) {
				System.out.println("現在値よりも低い値を入力してください。");
				System.out.print("損切り値:");
				setStopLoss(scanInt());
			}
			System.out.println("利確値：" + getTakeProfit() + "損切り値：" + getStopLoss() + "でよろしいですか？");
			System.out.println("はい：1 , 打ち直す：2 , プログラムを終了させる：3");
			select = scanInt();
			while (select > 3) {
				System.out.println("選択肢にある数字を入力してください。");
				select = scanInt();
			}
		} while (select == 2);
		switch (select) {
		case 1: //プログラム続行
			System.out.println("「はい」が選択されました。データベースに登録します。");
			break;
		case 3:
			System.out.println("「プログラムを終了させる」が選択されました。終了させます。");
			System.exit(0);
		}
		System.out.println("コメントを入力してください。なければ「なし」でEnterを押してください。");
		setComment(sc.next());
		setcreatedDay(sdFormat.format(Calendar.getInstance().getTime())); //更新日をセット
		//データベースにメソッドsqlAddStatementでレコードを追加する。
		insertRecord(sqlAddStatement(getProfile_idProfile(), getProfile_name(), getStockPrice_code(),
				getStockPrice_brand(), getTakeProfit(), getStopLoss(), getcreatedDay(), getComment()));
		//更新したものを参照する。
		sqlQuery(sqlSerchStatement());

		//推測の記入が完了したのでsqlを閉じる
		sqlClose();
	}

	public String getProfile_name() {
		return "'" + Profile_name + "'";
	}

	public void setProfile_name(String profile_name) {
		this.Profile_name = profile_name;
	}

	public int getProfile_idProfile() {
		return Profile_idProfile;
	}

	public void setProfile_idProfile(int profile_idProfile) {
		this.Profile_idProfile = profile_idProfile;
	}

	public String getStockPrice_brand() {
		return "'" + StockPrice_brand + "'";
	}

	public void setStockPrice_brand(String stockPrice_brand) {
		this.StockPrice_brand = stockPrice_brand;
	}

	public int getStockPrice_code() {
		return StockPrice_code;
	}

	public void setStockPrice_code(int stockPrice_code) {
		this.StockPrice_code = stockPrice_code;
	}

	public double getTakeProfit() {
		return takeProfit;
	}

	public void setTakeProfit(double takeProfit) {
		this.takeProfit = takeProfit;
	}

	public double getStopLoss() {
		return stopLoss;
	}

	public void setStopLoss(double stopLoss) {
		this.stopLoss = stopLoss;
	}

	public String getComment() {
		return "'" + comment + "'";
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getcreatedDay() {
		return "'" + createdDay + "'";
	}

	public void setcreatedDay(String createdDay) {
		this.createdDay = createdDay;
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
		}
		try {
			System.out.println("データベースに接続できました");
			statement = connect.createStatement();
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Statementクラスの生成に問題が発生しました。");
		}
	}

	//guessInputメソッドに使用。mydb.Predictデータ内にprfile_nameとcodeの組み合わせがいくつ入ってるか数える。
	public int sqlGuessCount() throws SQLException {
		ResultSet resultSet = null;
		resultSet = statement.executeQuery(sqlSerchCount());
		resultSet.next();
		return resultSet.getInt(1);

	}

	//mydb.Predictデータ内にprfile_nameとcodeの組み合わせがいくつ入ってるか数えるSQL文を作る。
	public String sqlSerchCount() {
		return "SELECT COUNT(*) FROM " + "mydb.Predict"
				+ " WHERE " + "Profile_idProfile " + " = " + getProfile_idProfile() + " AND "
				+ " StockPrice_code " + " = " + getStockPrice_code();
	}

	public int scanInt() {
		Scanner sc = new Scanner(System.in);
		int number = 0;
		int check = 0;
		do {
			try {
				number = sc.nextInt();
				while (number < 0) {
					System.out.println("正の整数を入力してください。");
					number = sc.nextInt();
				}
				check = 0;
			} catch (InputMismatchException e) {
				System.out.println(e);
				System.out.println("数字を打ってください。");
				sc.next();
				check = 1;
			}
		} while (check == 1);
		return number;
	}

	//テーブルに新たなレコードを入力
	public void insertRecord(String command) throws SQLException {
		// TODO 自動生成されたメソッド・スタブ
		int column;
		column = statement.executeUpdate(command);
		System.out.println("追加したカラムの数：" + column);
	}

	//新たなレコードを入力するためのSQL文を作る
	//株（銘柄）追加  table = "mydb.Predict"
	public String sqlAddStatement(int idProfile, String name, int code, String brand, double takeProfit, double stopLoss,
			String createdDay, String comment) {
		return "INSERT INTO " + " mydb.Predict "
				+ "(Profile_idProfile,Profile_name,StockPrice_code,StockPrice_brand,takeProfit,stopLoss,actFlag,createdDay,comment,intputFlag) "
				+ " VALUES " + "(" + idProfile + "," + name + "," + code + "," + brand + "," + takeProfit + ","
				+ stopLoss + "," + 1 + "," + createdDay + "," + comment + "," + 1 + ")";

	}

	//テーブルで検索するためのSQL文を作る
	public String sqlSerchStatement(String column, Object name) {
		return "SELECT * FROM " + "mydb.Predict"
				+ " WHERE " + column + " = " + name + " ";
	}
	//`idPredict`が一番大きいのが更新したレコードになる。挿入したレコードを検索するためのSQL文を作る
	public String sqlSerchStatement() {
		return "SELECT * FROM " + "mydb.Predict"
				+ " WHERE idPredict " + " IN " + "( SELECT MAX(idPredict) FROM mydb.Predict ) ";
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
