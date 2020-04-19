package webcrolling;

import static org.junit.Assert.fail;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;

public class DanawaWebCrolling extends JFrame {

	private WebDriver driver;
	
	private StringBuffer verificationErrors = new StringBuffer();
	private JLabel itemLabel = new JLabel("search data");
	private JTextField itemField = new JTextField(10);
	private JLabel conditionsLabel = new JLabel("weight(g)");
	private JTextField conditionsField = new JTextField(10);
	private JLabel priceBracketLabel = new JLabel("price bracket");// 가격범위
	private JTextField priceField1 = new JTextField(10);
	private JTextField priceField2 = new JTextField(10);
	private JLabel moniterSizeLabel = new JLabel("size(inch)");//인치
	private JTextField moniterSizeField = new JTextField(10);
	private Button startButton = new Button("start");
	private JTextArea statusTextArea = new JTextArea(30, 100);

	public DanawaWebCrolling() {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Danawa Web Crolling Program");
		setSize(1200, 600);
		Container c = getContentPane();
		setLayout(new FlowLayout());
		c.add(itemLabel);
		c.add(itemField);
		c.add(conditionsLabel);
		c.add(conditionsField);
		c.add(priceBracketLabel);
		c.add(priceField1);
		c.add(new JLabel("원"));
		c.add(new JLabel("~"));
		c.add(priceField2);
		c.add(new JLabel("원"));
		c.add(moniterSizeLabel);//2019.02.24 : 인치 라벨추가
		c.add(moniterSizeField);//2019.02.24 : 인치 필드 추가
		c.add(startButton);
		c.add(new JScrollPane(statusTextArea), BorderLayout.NORTH);
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (itemField.getText().length() == 0)
					return;
				if (conditionsField.getText().length() == 0)
					return;
				try {
					setUp();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				ServiceThread th = new ServiceThread();
				th.start();
				// search service thread start
			}
		});
		setVisible(true);
		revalidate();
		repaint();
	}

	public void setUp() throws Exception {
		System.setProperty("webdriver.chrome.driver", "C:\\chromedriver_win32\\chromedriver.exe");
		driver = new ChromeDriver();
		driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);// 웹페이지가 로딩될 때까지 20초 기다리겠다는 뜻
		driver.manage().timeouts().pageLoadTimeout(20, TimeUnit.SECONDS);
		driver.manage().window().maximize();

		driver.get("http://www.danawa.com/");
		driver.findElement(By.id("AKCSearch")).click();
		driver.findElement(By.id("AKCSearch")).clear();
		driver.findElement(By.id("AKCSearch")).sendKeys(itemField.getText());
		driver.findElement(By.xpath("//form[@id='srchFRM_TOP']/fieldset/div/button")).click();
		Thread.sleep(10000);// 다른 것이 안눌리도록 로딩될 때까지 대기
		driver.findElement(By.xpath("//div[@id='log_search_selectType']/select")).click();
		new Select(driver.findElement(By.xpath("//SELECT[@class='qnt_selector']"))).selectByVisibleText("90개");
		Thread.sleep(10000);// 위와 동일
		driver.findElement(By.id("priceRangeMinPrice")).click();
		driver.findElement(By.id("priceRangeMinPrice")).clear();
		driver.findElement(By.id("priceRangeMinPrice")).sendKeys(priceField1.getText());
		driver.findElement(By.id("priceRangeMaxPrice")).click();
		driver.findElement(By.id("priceRangeMaxPrice")).clear();
		driver.findElement(By.id("priceRangeMaxPrice")).sendKeys(priceField2.getText());
		driver.findElement(By.xpath("//BUTTON[@type='button'][text()='검색']")).click();
	}

	public void tearDown() throws Exception {
		driver.quit();
		String verificationErrorString = verificationErrors.toString();
		if (!"".equals(verificationErrorString)) {
			fail(verificationErrorString);
		}
	}

	public class ServiceThread extends Thread {
		private boolean isSpec_info = true;
		private String pro_name = null;
		private String spec_info = null;
		private String url = null;
		private String pricelist = null;
		private String price = null;
        private String inch = null;
		public void run() {
			int j = 1;// 페이지 배열 상수
			int pos = 0;
			while (true) {
				for (int i = 1; i < 91; i++) {
					try {
						pro_name = driver.findElement(By.xpath(XpathName.prod_name + "[" + Integer.toString(i) + "]"))
								.getText();
						url = driver.findElement(By.xpath(XpathName.prod_name + "[" + Integer.toString(i) + "]" + "//A"))
								.getAttribute("href");
						spec_info = driver.findElement(By.xpath(XpathName.spec_list + "[" + Integer.toString(i) + "]"))
								.getText();
						pricelist = driver.findElement(By.xpath(XpathName.prod_pricelist + "[" + Integer.toString(i) + "]"))
								.getText();
						
						if (spec_info.contains("Kg") || spec_info.contains("g")) {// Kg, g에 대한 정보
							StringTokenizer st = new StringTokenizer(spec_info, " ");
							while (st.hasMoreTokens()) {
								String info = st.nextToken();
								if (info.contains("Kg")) {
									String num = info.replace("Kg", "");
									Double weight = Double.parseDouble(num) * 1000;
									if (Double.parseDouble(conditionsField.getText()) > weight) {
										statusTextArea.append(
												j + "페이지: " + "[" + i + "]" + "무게:" + weight / 1000 + "Kg" + "\n");
										pos = statusTextArea.getText().length();
										statusTextArea.setCaretPosition(pos);

										statusTextArea
												.append(j + "페이지: " + "[" + i + "]" + "pro_name:" + pro_name + "\n");
										pos = statusTextArea.getText().length();
										statusTextArea.setCaretPosition(pos);

										statusTextArea
												.append(j + "페이지: " + "[" + i + "]" + "스펙 정보:" + spec_info + "\n");
										pos = statusTextArea.getText().length();
										statusTextArea.setCaretPosition(pos);

										statusTextArea.append(j + "페이지: " + "[" + i + "]" + "url:" + url + "\n");
										pos = statusTextArea.getText().length();
										statusTextArea.setCaretPosition(pos);
										
										StringTokenizer pt = new StringTokenizer(pricelist, "\r\n");
										while(pt.hasMoreTokens()) {
											String price = pt.nextToken();
											if(price.contains("원")) {
												statusTextArea.append(j + "페이지: " + "[" + i + "]" + "price:" + price + "\n");
												pos = statusTextArea.getText().length();
												statusTextArea.setCaretPosition(pos);
												break;
											}
										}
									}
								} else if (info.contains("g")) {
									String num = info.replace("g", "");
									Double weight = Double.parseDouble(num);
									if (Double.parseDouble(conditionsField.getText()) > weight) {
										statusTextArea.append(
												j + "페이지: " + "[" + i + "]" + "무게:" + weight / 1000 + "Kg" + "\n");
										pos = statusTextArea.getText().length();
										statusTextArea.setCaretPosition(pos);

										statusTextArea
												.append(j + "페이지: " + "[" + i + "]" + "pro_name:" + pro_name + "\n");
										pos = statusTextArea.getText().length();
										statusTextArea.setCaretPosition(pos);

										statusTextArea
												.append(j + "페이지: " + "[" + i + "]" + "스펙 정보:" + spec_info + "\n");
										pos = statusTextArea.getText().length();
										statusTextArea.setCaretPosition(pos);
									}
								}
							}
						}

					} catch (StaleElementReferenceException e) {
						System.out.println(e.getMessage());
						try {
							sleep(1000);// 아직 객체가 생성되지 않아 예외 발생할 시 1초후 다시 처리
						} catch (InterruptedException e1) {
							System.out.println(e1.getMessage());
						}
					} catch (NoSuchElementException e) {
						System.out.println(e.getMessage());
						statusTextArea.append("검색 완료. 서비스 종료\n");
						pos = statusTextArea.getText().length();
						statusTextArea.setCaretPosition(pos);
						return;
					} catch (NumberFormatException e) {
						System.out.println(e.getMessage());
					} finally {

					}
				}

				try {
					driver.findElement(By.linkText("다음 페이지")).click();
				} catch (NoSuchElementException e1) {
					System.out.println("No next");
					System.out.println(e1.getMessage());
				}
				j++;
			}

		}
	}

	public static void main(String[] args) {
		DanawaWebCrolling frame = new DanawaWebCrolling();
	}
}