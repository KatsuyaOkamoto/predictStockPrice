package predictStockPrice;
//株価を取得するためにネットからデータを拾う。

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.InputMismatchException;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class StockSerch {
	private String brand;
	private double openingPrice; //始値
	private double closingPrice; //終値
	private double highPrice; //高値
	private String highPriceTime; //高値の時間
	private double lowPrice; //安値
	private String lowPriceTime; //安値の時間
	private String updateDay; //更新日
	static SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
	static Calendar cl = Calendar.getInstance();

	//株銘柄データをsqlに登録する前に株の数値を取得する。
	public void reserchStock(int stockNumber) throws IOException {
		// ****のページにアクセス
		Document document = Jsoup
				.connect("https:/****l?QCODE=" + stockNumber
						+ "&TEMPLATE=****")
				.get();
		//.title = 銘柄、.tbl 1行目 = 終値、.tbl 2行目 = 始値, 高値, 高値の時間, 安値, 安値の時間
		Elements elements = document.select(".title, .tbl");
//		System.out.println(elements.get(2).text());
		String title = elements.get(0).text();
		String presentValueText;	//終値
		String cutOutValue;	//始値, 高値, 高値の時間, 安値, 安値の時間
			presentValueText = elements.get(1).text();
			cutOutValue = elements.get(2).text();
		System.out.println("コード [" + stockNumber + "]の銘柄は「" + title + "」です。");
		System.out.println("これを登録しますか？ はい: 1 , いいえ（やり直す）: 2");
		Scanner sc = new Scanner(System.in);
		int select = 0;
		do {
			try {
				select = sc.nextInt();
			} catch (InputMismatchException e) {
				//数字を打った場合、やり直せなくなる。
				System.out.println("数字意外を打ったからエラー何だよね！");
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
		setBrand(title); //銘柄をセット
		setClosingPrice(getFigure(presentValueText, "現在値")); //終値をセット
		setOpeningPrice(getFigure(cutOutValue, "始値")); //始値をセット
		setHighPrice(getFigure(cutOutValue, "高値")); //高値をセット
		setLowPrice(getFigure(cutOutValue, "安値")); //安値をセット
		try {
			setHighPriceTime(getDate(cutOutValue, "高値")); //高値の時間をセット
			setLowPriceTime(getDate(cutOutValue, "安値")); //安値の時間をセット
		} catch (ParseException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		System.out.println(getHighPriceTime());
		setUpdateDay(sdFormat.format(cl.getTime())); //更新日をセット
		System.out.println(getUpdateDay());

		for (Element element : elements) {
			System.out.println(element.text());
		}
		sc.close();

	}

	//登録している株の情報をアップデートする。
	public void updateReserchStock(int stockNumber) throws IOException {
		// ****のページにアクセス
		Document document = Jsoup
				.connect("https:/****l?QCODE=" + stockNumber
				+ "&TEMPLATE=****")
				.get();
		//.title = 銘柄、.tbl 1行目 = 終値、.tbl 2行目 = 始値, 高値, 高値の時間, 安値, 安値の時間
		Elements elements = document.select(".title, .tbl");
		String title = elements.get(0).text();
		System.out.println(title);
		String presentValueText = elements.get(1).text();
		System.out.println(presentValueText);
		String cutOutValue = elements.get(2).text();
		System.out.println(cutOutValue);

		setBrand(title); //銘柄をセット
		setClosingPrice(getFigure(presentValueText, "現在値")); //終値をセット
		setOpeningPrice(getFigure(cutOutValue, "始値")); //始値をセット
		setHighPrice(getFigure(cutOutValue, "高値")); //高値をセット
		setLowPrice(getFigure(cutOutValue, "安値")); //安値をセット
		try {
			setHighPriceTime(getDate(cutOutValue, "高値")); //高値の時間をセット
			setLowPriceTime(getDate(cutOutValue, "安値")); //安値の時間をセット
		} catch (ParseException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		setUpdateDay(sdFormat.format(cl.getTime())); //更新日をセット

		for (Element element : elements) {
			System.out.println(element.text());
		}


	}

	//取得したテキストから数値を取得するメソッドを作成
	public static double getFigure(String text, String name) {
		int setPoint1 = text.indexOf(name);
		String cutText = text.substring(setPoint1 + name.length() + 1); //+1は余白分。 例：name = "高値"の場合、高値より後の文字列をとる。
				System.out.println(cutText);
		cutText = cutText.replace(",", ""); 	//","を排除することにより返しで数値を獲得することができる。
		int setPoint2 = cutText.indexOf(" ");
				System.out.println(setPoint2);
		if (name == "現在値")
			setPoint2--; //現在値にある↓↑・を排除
		String value = cutText.substring(0, setPoint2); //数値をString型で取得　例：name = "高値"の場合、高値より後でその先の空白より前の文字列（数値）をとる。
		return Double.parseDouble(value); //String型をdouble型に変換
	}

	//高値または低値の時間を取得
	public static String getDate(String text, String name) throws ParseException {
		int setPoint1 = text.indexOf(name);
		String cutText = text.substring(setPoint1 + name.length() + 1); //+1は余白分。
				System.out.println(cutText);
		int setPoint2 = cutText.indexOf("(");
		int setPoint3 = cutText.indexOf(")");
		String value = cutText.substring(setPoint2 + 1, setPoint3); //数値をString型で取得
		System.out.println(value);
		return value;
	}

	public String getBrand() {
		return "'" +  brand + "'";
	}

	public double getOpeningPrice() {
		return openingPrice;
	}

	public double getClosingPrice() {
		return closingPrice;
	}

	public double getHighPrice() {
		return highPrice;
	}

	public String getHighPriceTime() {
		return "'" + highPriceTime + "'";
	}

	public double getLowPrice() {
		return lowPrice;
	}

	public String getLowPriceTime() {
		return "'" + lowPriceTime + "'";
	}

	public String getUpdateDay() {
		return "'" + updateDay + "'";
	}

	private void setUpdateDay(String updateDay) {
		this.updateDay = updateDay;
	}

	private void setBrand(String brand) {
		this.brand = brand;
	}

	private void setOpeningPrice(double openingPrice) {
		this.openingPrice = openingPrice;
	}

	private void setClosingPrice(double closingPrice) {
		this.closingPrice = closingPrice;
	}

	private void setHighPrice(double highPrice) {
		this.highPrice = highPrice;
	}

	private void setHighPriceTime(String highPriceTime) {
		this.highPriceTime = highPriceTime;
	}

	private void setLowPrice(double lowPrice) {
		this.lowPrice = lowPrice;
	}

	private void setLowPriceTime(String lowPriceTime) {
		this.lowPriceTime = lowPriceTime;
	}

}
