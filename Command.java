package predictStockPrice;

import java.io.IOException;
import java.sql.SQLException;
import java.util.InputMismatchException;
import java.util.Scanner;
//メニューを開き、プロフィール登録や予想登録、強制的にクローリングをさせる。
public class Command {
	public static void main(String[] args) {
		menuView();
	}
	public static void menuView() {
		Scanner sc = new Scanner(System.in);
		String name;	//プロフィールの名前
		int code;
		System.out.println("以下から選び、数字を入力してください。");
		System.out.println("1:プロフィール登録");
		System.out.println("2:株価推測登録(プロフィール登録を行なっているnameから推測登録できます。)");
		System.out.println("3:強制クローリング(株価及び推測値を更新します。)");
//		System.out.println("4:プロフィール検索、現在の勝率をCSVファイルに出力");
		switch(sc.nextInt()) {
		case 1:
			Profile profileRegist = new Profile();
			profileRegist.sqlConnect("jdbc:mysql://127.0.0.1:3306", "root", "");	//hostName(ホスト), root(権限), pass(パスワード)を入力
			System.out.println("登録する名前を入力してください。");
			name = sc.next();
			profileRegist.insertRecord(profileRegist.sqlAddStatement(name));
			profileRegist.sqlQuery(profileRegist.sqlSerchStatement("name",name));
			break;
		case 2:
			GuessSystem guess = new GuessSystem();
			try {
				System.out.println("推測を行うプロフィールネームを入力してください。");
				name = sc.next();
				System.out.println("推測する株の銘柄コード4桁を入力してください。");
				code = scanInt();
				guess.guessInput(name, code);
			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
			break;
		case 3:
			CrowPredictDBWrite write = new CrowPredictDBWrite();
			try {
				write.run();
			} catch (SQLException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
			break;
		}

	}

	public static int scanInt() {
		Scanner sc = new Scanner(System.in);
		int number = 0;
		int check = 0;
		do {
			try {
				number = sc.nextInt();
				check = 0;
			} catch (InputMismatchException e) {
				System.out.println("数字意外を打ったからエラーだよね！");
				System.out.println(e);
				System.out.println("数字を打ってください。");
				sc.next();
				check = 1;
			}
		} while (check == 1);

		return number;

	}

}
