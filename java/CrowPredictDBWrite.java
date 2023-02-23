package predictStockPrice;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
//プログラム自動実行を使い、株価更新と株価予想のクローリングを行う
//runメソッド利用で一通りクローリングを行うことができる。
public class CrowPredictDBWrite extends GuessSystem{
	private ArrayList<Integer> brandArray = new ArrayList<Integer>();	//株の銘柄コードを集めて配列にしたもの
	private ArrayList<ArrayList<Object>> infoArrays = new ArrayList<ArrayList<Object>>();	//mydb.Predictにあるレコードを纏めて配列
	ArrayList<Object> infoArray = new ArrayList<Object>();	//mydb.Predictにあるレコードの内、{idPredict, Profile_name, takeProfit, stopLoss, createdDay}を抽出する。
	private Map<String, Object> priceMap = new HashMap<String, Object>(); //{("高値", 数値),("安値", 数値)}
	private double gains;	//始値から利確した時の利益
	private double loss;	//始値から損切りした時の損失

	//TimeStampクラスupdateCheckメソッドからまだクローリングをしていないか確認する。（true(日付の一致)の場合プログラムを閉じる）
	//クローリングするべき銘柄選定し、StockPriceクラスのupdateStockPriceメソッドからデータ（`StockPrice`）にある株価を更新する。
	//次に銘柄を一つ取り、その銘柄を推測しているプロフィールネームを配列でとる。
	//次に株価からmydb.Predictにある予想値を比較してresultPriceに代入する。
	//これを一通り繰り返す。終わったらTimeStampクラスwriteUpdateメソッドでタイムスタンプを更新する。
	public void run() throws SQLException {
		TimeStamp timeStamp = new TimeStamp();
		if(timeStamp.updateCheck()) {	//true =　lastUpdateが本日と一致している
			System.out.println("既にクローリングは終えてます。プログラムを終了します。");
			System.exit(0);
		}
		getCrawlingBrand();	//クローリングするべき銘柄を選定する。
		StockPrice crowStock = new StockPrice();
		for(int brandCount = 0; brandCount < brandArray.size(); brandCount++) {
			crowStock.updateStockPrice(getBrandArray(brandCount));	//株価を一つずつ更新
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
			getCrawLingInfo(brandCount);	//その銘柄を推測しているプロフィールネームを配列でとる
			highLowPrice(brandCount);	//一つの銘柄の株価（高値と安値）を取得する。
			for(int roopCount = 0; roopCount < infoArrays.size(); roopCount++) {
				gainsAndLoss(roopCount) ;
				if(timeStamp.createGap((String) infoArrays.get(roopCount).get(5)) && judgement(roopCount, (Double)getPriceMap("highPrice"), (Double)getPriceMap("lowPrice"))) {
					predictAssignment(roopCount, (Double)getPriceMap("highPrice"), (String)getPriceMap("highPriceTime"), (Double)getPriceMap("lowPrice"), (String)getPriceMap("lowPriceTime")) ;
				}
			}
		}
		timeStamp.writeUpdate();
		sqlClose();
	}
//クローリングするべき銘柄を選定する。
	public void getCrawlingBrand() throws SQLException {
		sqlConnect("jdbc:mysql://127.0.0.1:3306", "root", "");
		ResultSet result = null;
		//mydb.Predictからクローリングする（actFlag = 1）株の銘柄コードを取得する。
		result = statement.executeQuery("SELECT StockPrice_code FROM mydb.Predict WHERE actFlag = 1 GROUP BY StockPrice_code");
		while(result.next()) {
			setBrandArray(result.getInt(1));	//株の銘柄コードを一つずつbrandArrayに追加している。
		}
	}
//その銘柄を推測しているプロフィールネームを配列でとる。
	public void getCrawLingInfo(int brandCount) throws SQLException {	//brandCountは株の銘柄コードを抽出する番号
		ResultSet result = null;
		//mydb.Predictから銘柄を予測している番号（idPredict）、プロフィールネーム（Profile_name）、利確値（takeProfit）、損切り値（stopLoss）、損益算出フラグ（intputFlag）を抽出する。
		result = statement.executeQuery("SELECT idPredict, Profile_name, takeProfit, stopLoss, intputFlag, createdDay FROM mydb.Predict WHERE StockPrice_code = " + getBrandArray(brandCount) + " AND actFlag = 1");
		while(result.next()) {
			infoArray.add(result.getInt(1));	//番号（idPredict）	取り出すときは(Integer)infoArray.get(0)
			infoArray.add(result.getString(2));	//プロフィールネーム（Profile_name）	取り出すときは(String)infoArray.get(1)
			infoArray.add(result.getDouble(3));	//利確値（takeProfit）	取り出すときは(Double)infoArray.get(2)
			infoArray.add(result.getDouble(4));	//損切り値（stopLoss）		取り出すときは(Double)infoArray.get(3)
			infoArray.add(result.getInt(5));	//損益算出フラグ（intputFlag）	取り出すときは(Integer)infoArray.get(4)
			infoArray.add(result.getString(6)); //作成日(createdDay) 取り出すときは(Integer)infoArray.get(5)
//			System.out.println(infoArray);
			setInfoArrays(infoArray);	//infoArrayをsetInfoArraysに追加
			infoArray = new ArrayList<Object>();	//infoArrayを初期化し次の情報を挿入
//			System.out.println(infoArrays);
		}
	}
//一つの銘柄の株価（高値と安値）を取得する。
	public void highLowPrice(int brandRoopCount) throws SQLException {	//roopCountは株の銘柄コードを抽出する番号
		ResultSet result = null;
		result = statement.executeQuery("SELECT openingPrice, highPrice, highPriceTime, lowPrice, lowPriceTime FROM mydb.StockPrice WHERE code = " + getBrandArray(brandRoopCount));
		result.next();
		setPriceMap("openingPrice", result.getDouble(1));
		setPriceMap("highPrice", result.getDouble(2));
		setPriceMap("highPriceTime", result.getString(3));
		setPriceMap("lowPrice", result.getDouble(4));
		setPriceMap("lowPriceTime", result.getString(5));
	}
//intputFlag = 1かどうか判定し、株価の始値からmydb.Predictにあるgainsとlossを算出する。
	public void gainsAndLoss(int roopCount) throws SQLException {	//roopCountは予測するレコードを抽出する番号
		//infoArraysの中から順番（roopCount（0から））にinfoArrayからinputFlagが１であるかどうか判定する。
		if(getInfoArrays(roopCount).get(4).equals(1)) {
			System.out.println("true");
			setGains(((Double)getInfoArrays(roopCount).get(2) - (Double)getPriceMap("openingPrice")) * 100);	//利益値をセット
			setLoss(((Double)getInfoArrays(roopCount).get(3) - (Double)getPriceMap("openingPrice")) * 100);		//損失値をセット
			if(getGains() <= 0 || getLoss() >= 0) {	//利益（gains）がマイナスまたは損失（loss）がプラスの場合、記録は無効とする。gainsとlossの値を０にする。
				System.out.println("無効が発生");
				setGains(0);
				setLoss(0);
				int num = statement.executeUpdate("UPDATE mydb.Predict SET gains = " + getGains() + ", loss = " + getLoss() + ", comment = '無効', intputFlag = 0 WHERE idPredict = " + getInfoArrays(roopCount).get(0));
			}else {
				int num = statement.executeUpdate("UPDATE mydb.Predict SET gains = " + getGains() + ", loss = " + getLoss() + ", intputFlag = 0 WHERE idPredict = " + getInfoArrays(roopCount).get(0));
			}
		}
		ResultSet result = null;
		result = statement.executeQuery("SELECT gains, loss FROM mydb.Predict WHERE idPredict = " + getInfoArrays(roopCount).get(0));
		result.next();
		setGains(result.getDouble(1));	//利益値をセット
		setLoss(result.getDouble(2));	//損失値をセット

	}
//株価から利確、損切りが発生するか確認する。
	public boolean judgement(int roopCount, double highPrice, double lowPrice) throws SQLException {
		if(highPrice >= (Double)getInfoArrays(roopCount).get(2)) {	//高値が利確値よりも超えた場合
			return true;	//trueの場合、predictAssignmentメソッドを起動
		}else if(lowPrice <= (Double)getInfoArrays(roopCount).get(3)){	//安値が損切り値よりも下がった場合
			return true;	//trueの場合、predictAssignmentメソッドを起動
		}
		return false;	//falseの場合、次のループに入る
	}
//株価から利確損切りが発生した場合mydb.PredictにあるactFlag(クローリングのオンオフ)を0にし、resultPriceに損益を入れる。
	public void predictAssignment(int roopCount, double highPrice, String highPriceTime, double lowPrice, String lowPriceTime) throws SQLException {	//roopCountは予測するレコードを抽出する番号
		double resultPrice = 0;	//確定値
		double arrayHighPrice = (Double)getInfoArrays(roopCount).get(2);	//利確値
		double arrayLowPrice = (Double)getInfoArrays(roopCount).get(3);		//損切り値
		final String FORMAT = "HH:mm:ss";
		//その日に高値と安値で利確（arrayHighPrice）と損切り（arrayLowPrice）がどちらも発生した場合
		if(highPrice >= arrayHighPrice && lowPrice <= arrayLowPrice){
			LocalTime highTime = LocalTime.parse(highPriceTime, DateTimeFormatter.ofPattern(FORMAT));	//株価の高値の時間をセット
			LocalTime lowTime = LocalTime.parse(highPriceTime, DateTimeFormatter.ofPattern(FORMAT));	//株価の安値の時間をセット
			if(highTime.isBefore(lowTime)) {	//高値の時間が安値の時間よりも早い場合、利確で記録する。
				resultPrice = getGains();
			}else if(lowTime.isBefore(highTime)) {	//安値の時間が高値の時間よりも早い場合、損切りで記録する。
				resultPrice = getLoss();
			}
		//以下利確のみ、損切りのみ発生した場合
		}else if(highPrice >= arrayHighPrice) {
			resultPrice = getGains();
		}else if(lowPrice <= arrayLowPrice) {
			resultPrice = getLoss();
		}
		//mydb.Predictに記録する。
		int num  = statement.executeUpdate("UPDATE mydb.Predict SET resultPrice = " + resultPrice + ", actFlag = 0 WHERE idPredict = " + getInfoArrays(roopCount).get(0));
	}
	public int getBrandArray(int roopCount) {
		return brandArray.get(roopCount);
	}
	private void setBrandArray(int brandCode) {
		this.brandArray.add(brandCode);
	}

	//infoArray = {番号（idPredict）、プロフィールネーム（Profile_name）、利確値（takeProfit）、損切り値（stopLoss）}
	public ArrayList<Object> getInfoArrays(int order) {
		return infoArrays.get(order);	//要素(order)にあるinfoArrayを抽出する
	}
	private void setInfoArrays(ArrayList<Object> infoArray) {
		this.infoArrays.add(infoArray);
	}
	public Object getPriceMap(String title) {
		return priceMap.get(title);
	}
	private void setPriceMap(String title, Object price) {
		this.priceMap.put(title, price);	//株価におけるtitleにセットするキーはopeningPrice, highPrice, highPriceTime, lowPrice, lowPriceTime
	}
	public double getGains() {
		return gains;
	}
	private void setGains(double gains) {
		this.gains = gains;
	}
	public double getLoss() {
		return loss;
	}
	private void setLoss(double loss) {
		this.loss = loss;
	}


}
