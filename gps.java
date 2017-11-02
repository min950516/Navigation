import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.PriorityQueue;

import javax.swing.*;
import net.sf.marineapi.nmea.event.SentenceEvent;
import net.sf.marineapi.nmea.event.SentenceListener;
import net.sf.marineapi.nmea.io.SentenceReader;
import net.sf.marineapi.nmea.sentence.SentenceValidator;

public class gps implements SentenceListener {

	static float latitude = (float) 37.583800;
	static float longitude = (float) 127.008400;
	static Float[] lat = new Float[90];// 학교의 세로 위도 저장
	static Float[] lon = new Float[100];// 학교의 가로 위도 저장
	GridLayout position = new GridLayout(90, 100);// GridLayout설정
	static JPanel Mposition[][] = new JPanel[90][100];// JPanel에 색깔을 정하기위한 배열 저장
	static int verposition;// 위도배열위치 저장값
	static int horiposition;// 경도배열위치 저장값
	JLabel background = new JLabel(new ImageIcon("hansung.png"));// 학교 지도 이미지 저장
	float latresult = (float) 0.000000;// 보간법을 위한 자신의 위치 경도값
	float lonresult = (float) 0.000000;// 보간법을 위한 자신의 위치 위도값
	// positioning

	JMenu submenu;
	JMenu submenu2;
	int hour;
	int minute;
	int second;
	int count;
	String message = "";
	boolean is_msg_start = false;

	JFrame frame = new JFrame();
	String parsing;

	public static final int DIAGONAL_COST = 14;
	public static final int V_H_COST = 10;
	static PriorityQueue<Cell> open;

	static boolean closed[][];
	static int startI, startJ;
	static int endI, endJ;
	static int[][] block;

	static Cell[][] grid = new Cell[90][100];

	public gps() {
		init();
	}

	public void readingPaused() {
		System.out.println("-- Paused --");
	}

	public void readingStarted() {
		System.out.println("-- Started --");
	}

	public void readingStopped() {
		System.out.println("-- Stopped --");
	}

	public void sentenceRead(SentenceEvent event) {

		System.out.println(event.getSentence());
		parsing = event.getSentence().toString();
		String[] values = parsing.split(",|[*]");
		if (values[0].equals("$GPGGA")) {
			for (int x = 0; x < values.length; x++) {
				if (!values[x].equals("")) {
					if (x == 1) {
						String[] time = values[1].split("");
						hour = Integer.parseInt(time[0] + time[1]) + 9;
						minute = Integer.parseInt(time[2] + time[3]);
						second = Integer.parseInt(time[4] + time[5]);
						if (hour >= 24) {
							hour -= 24;
						}

					} else if (x == 2) {
						String[] parselat = values[2].split("");
						Integer ten1 = Integer.parseInt(parselat[0] + parselat[1]);
						Float one1 = Float.parseFloat(parselat[2] + parselat[3] + parselat[4] + parselat[5]
								+ parselat[6] + parselat[7] + parselat[8]);
						latresult = (float) (ten1 + (one1 / 60));

						for (int i = 0; i < 89; i++) {

							if ((float) lat[i] >= (float) latresult && (float) latresult > (float) lat[i + 1]) {
								System.out.println("aslkdjflkjsdlf");
								verposition = i;
							}
						} // 몇번째 위도값에 있나?
					} else if (x == 4) {

						String[] parselon = values[4].split("");
						Integer ten2 = Integer.parseInt(parselon[0] + parselon[1] + parselon[2]);
						Float one2 = Float.parseFloat(parselon[3] + parselon[4] + parselon[5] + parselon[6]
								+ parselon[7] + parselon[8] + parselon[9]);
						lonresult = (float) (ten2 + (one2 / 60));

						for (int i = 0; i < 99; i++) {
							if ((float) lon[i] <= (float) lonresult && (float) lonresult < (float) lon[i + 1]) {
								horiposition = i;
							}
						} // 몇번째 경도값에 있나?
							// }

						// for(int i=0; i<90; i++){
						// for(int j=0; j<100; j++){

						// Mposition[i][j].setVisible(false);
						// test(1, 90,100, 5, 5, 80, 90, block);
						// num,세로크기,가로크기, 시작점y,시작점x,도착점y,도착점x, 장애물 처리
						// if(i==verposition && j==horiposition){

						Mposition[verposition][horiposition].setVisible(true);
						Mposition[verposition][horiposition].setBackground(Color.red);
						// }
						// }
						// }
						System.out.println(verposition);
						System.out.println(horiposition);
						System.out.println(latresult);
						System.out.println(lonresult);
						System.out.println((x + 1) + " : " + values[x]);
					}
				}
			}
		} else if (values[0].equals("$GPGLL")) {
			for (int x = 0; x < values.length; x++) {
				if (!values[x].equals("")) {
					if (x == 1) {
						// label2.setText("위도 = " + values[1] + values[2]);
					} else if (x == 3) {
						// label3.setText("경도 = " + values[3] + values[4]);
					}
					System.out.println("문자(열) " + (x + 1) + " : " + values[x]);
				}
			}
		} else if (values[0].equals("$GPGSA")) {
			for (int x = 0; x < values.length; x++) {
				if (!values[x].equals("")) {
					if (x == 2) {
						// label7.setText("MODE = " + values[2]);
					} else if (x == 15) {
						// label5.setText("PDOP = " + values[15]);
					} else if (x == 16) {
						// label4.setText("HDOP = " + values[16]);
					} else if (x == 17) {
						// label6.setText("VDOP = " + values[17]);
					} else if (x == 3) {
						// label12.setText("위성 종류 = " + values[3] + " | " +
						// values[4] + " | " + values[5] + " | "
						// + values[6] + " | " + values[7] + " | " + values[8] +
						// " | " + values[9] + " | "
						// + values[10] + " | " + values[11] + " | " +
						// values[12] + " | " + values[13] + " | "
						// + values[14]);
					}
				}
				System.out.println((x + 1) + " : " + values[x]);
			}
		} else if (values[0].equals("$GPGSV")) {
			for (int x = 0; x < values.length; x++) {
				if (!values[x].equals("")) {

				}
				System.out.println(+(x + 1) + " : " + values[x]);

			}
		} else if (values[0].equals("$GPRMC")) {
			for (int x = 0; x < values.length; x++) {
				if (!values[x].equals("")) {
					if (x == 9) {
						String[] date = values[9].split("");
						int day = Integer.parseInt(date[0] + date[1]);
						int month = Integer.parseInt(date[2] + date[3]);
						int year = Integer.parseInt(date[4] + date[5]) + 2000;

					}
				}
				System.out.println((x + 1) + " : " + values[x]);
			}
		} else if (values[0].equals("$GPVTG")) {
			for (int x = 0; x < values.length; x++) {
				if (!values[x].equals("")) {

				}
				System.out.println((x + 1) + " : " + values[x]);
			}
		}
	}

	private SerialPort getSerialPort() {
		try {
			Enumeration<?> e = CommPortIdentifier.getPortIdentifiers();

			while (e.hasMoreElements()) {
				CommPortIdentifier id = (CommPortIdentifier) e.nextElement();

				if (id.getPortType() == CommPortIdentifier.PORT_SERIAL) {

					SerialPort sp = (SerialPort) id.open("SerialExample", 30);

					sp.setSerialPortParams(4800, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

					InputStream is = sp.getInputStream();
					InputStreamReader isr = new InputStreamReader(is);
					BufferedReader buf = new BufferedReader(isr);

					System.out.println("Scanning port " + sp.getName());

					for (int i = 0; i < 5; i++) {
						try {
							String data = buf.readLine();
							if (SentenceValidator.isValid(data)) {
								System.out.println("NMEA data found!");
								return sp;
							}
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					is.close();
					isr.close();
					buf.close();
				}
			}
			System.out.println("NMEA data was not found..");

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private void init() {
		try {
			SerialPort sp = getSerialPort();

			if (sp != null) {
				InputStream is = sp.getInputStream();
				SentenceReader sr = new SentenceReader(is);
				sr.addSentenceListener(this);
				sr.start();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void GPSframe() {

		JMenuBar mb = new JMenuBar();
		JMenu mf1 = new JMenu("메뉴");
		JMenuItem n1 = new JMenuItem("초기화");
		JMenuItem n2 = new JMenuItem("끝내기");

		JMenu mf2 = new JMenu("기능");
		submenu = new JMenu("길찾기");
		submenu2 = new JMenu("검색");
		JMenuItem m3 = new JMenuItem("현재시간");

		submenu.add(new JMenuItem("탐구관")).addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (int i = 0; i < 90; i++) {
					for (int j = 0; j < 100; j++) {
						Mposition[i][j].setVisible(false);
					}
				}
				System.out.println("탐구관을 선택하였습니다.");
				// test(1, 90, 100, 40, 40, 14, 19, block);
				test(1, 90, 100, verposition, horiposition, 14, 19, block);
			}
		});
		submenu.add(new JMenuItem("진리관")).addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (int i = 0; i < 90; i++) {
					for (int j = 0; j < 100; j++) {
						Mposition[i][j].setVisible(false);
					}
				}
				System.out.println("진리관을 선택하였습니다.");
				test(1, 90, 100, verposition, horiposition, 28, 30, block);
				// test(1, 90, 100, 40, 40, 28, 30, block);
			}
		});
		submenu.add(new JMenuItem("우촌관")).addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (int i = 0; i < 90; i++) {
					for (int j = 0; j < 100; j++) {
						Mposition[i][j].setVisible(false);
					}
				}
				System.out.println("우촌관을 선택하였습니다.");
				test(1, 90, 100, verposition, horiposition, 27, 52, block);
				// test(1, 90, 100, 40, 40, 27, 52, block);
			}
		});
		submenu.add(new JMenuItem("연구관")).addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (int i = 0; i < 90; i++) {
					for (int j = 0; j < 100; j++) {
						Mposition[i][j].setVisible(false);
					}
				}
				System.out.println("연구관을 선택하였습니다.");
				test(1, 90, 100, verposition, horiposition, 53, 36, block);
				// test(1, 90, 100, 40, 40, 53, 36, block);
			}
		});

		submenu.add(new JMenuItem("지선관")).addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (int i = 0; i < 90; i++) {
					for (int j = 0; j < 100; j++) {
						Mposition[i][j].setVisible(false);
					}
				}
				System.out.println("지선관을 선택하였습니다.");
				test(1, 90, 100, verposition, horiposition, 56, 35, block);
				// test(1, 90, 100, 40, 40, 56, 35, block);
			}
		});
		submenu.add(new JMenuItem("공학관")).addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (int i = 0; i < 90; i++) {
					for (int j = 0; j < 100; j++) {
						Mposition[i][j].setVisible(false);
					}
				}
				System.out.println("공학관을 선택하였습니다.");
				test(1, 90, 100, verposition, horiposition, 63, 38, block);
				// test(1, 90, 100, 40, 40, 63, 38, block);
			}
		});

		submenu.add(new JMenuItem("창의관")).addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (int i = 0; i < 90; i++) {
					for (int j = 0; j < 100; j++) {
						Mposition[i][j].setVisible(false);
					}
				}
				System.out.println("창의관을 선택하였습니다.");
				test(1, 90, 100, verposition, horiposition, 50, 62, block);
				// test(1, 90, 100, 40, 40, 50, 62, block);
			}
		});
		submenu.add(new JMenuItem("미래관")).addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (int i = 0; i < 90; i++) {
					for (int j = 0; j < 100; j++) {
						Mposition[i][j].setVisible(false);
					}
				}
				System.out.println("미래관을 선택하였습니다.");
				test(1, 90, 100, verposition, horiposition, 45, 60, block);
				// test(1, 90, 100, 40, 40, 45, 60, block);
			}
		});
		submenu.add(new JMenuItem("낙산관")).addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (int i = 0; i < 90; i++) {
					for (int j = 0; j < 100; j++) {
						Mposition[i][j].setVisible(false);
					}
				}
				System.out.println("낙산관을 선택하였습니다.");
				test(1, 90, 100, verposition, horiposition, 50, 76, block);
				// test(1, 90, 100, 40, 40, 50, 76, block);
			}
		});

		submenu2.add(new JMenuItem("탐구관")).addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Mposition[14][19].setVisible(true);
				Mposition[14][19].setBackground(Color.green);
			}
		});
		submenu2.add(new JMenuItem("진리관")).addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Mposition[28][30].setVisible(true);
				Mposition[28][30].setBackground(Color.green);
			}
		});
		submenu2.add(new JMenuItem("우촌관")).addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Mposition[27][52].setVisible(true);
				Mposition[27][52].setBackground(Color.green);
			}
		});
		submenu2.add(new JMenuItem("연구관")).addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Mposition[53][36].setVisible(true);
				Mposition[53][36].setBackground(Color.green);
			}
		});

		submenu2.add(new JMenuItem("지선관")).addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Mposition[56][35].setVisible(true);
				Mposition[56][35].setBackground(Color.green);
			}
		});
		submenu2.add(new JMenuItem("공학관")).addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Mposition[63][38].setVisible(true);
				Mposition[63][38].setBackground(Color.green);
			}
		});

		submenu2.add(new JMenuItem("창의관")).addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Mposition[50][62].setVisible(true);
				Mposition[50][62].setBackground(Color.green);
			}
		});
		submenu2.add(new JMenuItem("미래관")).addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Mposition[45][60].setVisible(true);
				Mposition[45][60].setBackground(Color.green);
			}
		});
		submenu2.add(new JMenuItem("낙산관")).addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Mposition[50][76].setVisible(true);
				Mposition[50][76].setBackground(Color.green);
			}
		});

		// Container con = frame.getContentPane();
		mf1.add(n1);
		mf1.add(n2);
		mf2.add(submenu);
		mf2.add(submenu2);
		mf2.add(m3);

		n1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (int i = 0; i < 90; i++) {
					for (int j = 0; j < 100; j++) {
						Mposition[i][j].setVisible(false);
					}
				}
			}
		});
		// 초기화
		n2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		// 끝내기

		m3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				JOptionPane.showMessageDialog(null, "현재 시간 : " + hour + "시 " + minute + "분 ", "현재시간",
						JOptionPane.PLAIN_MESSAGE);

			}
		});

		frame.setJMenuBar(mb);
		mb.add(mf1);
		mb.add(mf2);

		frame.setTitle("학교내 길찾기 서비스 1494027 이건녕/1494022 서정민");
		frame.setLayout(new BorderLayout());

		frame.add(background, BorderLayout.CENTER);
		background.setLayout(position);

		for (int i = 0; i < 90; i++) {
			for (int j = 0; j < 100; j++) {
				Mposition[i][j] = new JPanel();
				Mposition[i][j].setVisible(false);
			}
		}

		for (int i = 0; i < 90; i++) {
			for (int j = 0; j < 100; j++) {
				background.add(Mposition[i][j]);
			}
		}

		frame.getContentPane().setBackground(Color.white);
		frame.setSize(1100, 990);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	static class Cell {
		int heuristicCost = 0; // Heuristic cost
		int finalCost = 0; // G+H
		int i, j;
		Cell parent;

		Cell(int i, int j) {
			this.i = i;
			this.j = j;
		}

		@Override
		public String toString() {
			return "[" + this.i + ", " + this.j + "]";
		}
	}

	public static void setBlocked(int i, int j) {
		grid[i][j] = null;
	}

	public static void setStartCell(int i, int j) {
		startI = i;
		startJ = j;
	}

	public static void setEndCell(int i, int j) {
		endI = i;
		endJ = j;
	}

	static void checkAndUpdateCost(Cell current, Cell t, int cost) {
		if (t == null || closed[t.i][t.j])
			return;
		int t_final_cost = t.heuristicCost + cost;

		boolean inOpen = open.contains(t);
		if (!inOpen || t_final_cost < t.finalCost) {
			t.finalCost = t_final_cost;
			t.parent = current;
			if (!inOpen)
				open.add(t);
		}
	}

	public static void AStar() {

		open.add(grid[startI][startJ]);

		Cell current;

		while (true) {
			current = open.poll();
			if (current == null)
				break;
			closed[current.i][current.j] = true;

			if (current.equals(grid[endI][endJ])) {
				return;
			}

			Cell t;
			if (current.i - 1 >= 0) {
				t = grid[current.i - 1][current.j];
				checkAndUpdateCost(current, t, current.finalCost + V_H_COST);

				if (current.j - 1 >= 0) {
					t = grid[current.i - 1][current.j - 1];
					checkAndUpdateCost(current, t, current.finalCost + DIAGONAL_COST);
				}

				if (current.j + 1 < grid[0].length) {
					t = grid[current.i - 1][current.j + 1];
					checkAndUpdateCost(current, t, current.finalCost + DIAGONAL_COST);
				}
			}

			if (current.j - 1 >= 0) {
				t = grid[current.i][current.j - 1];
				checkAndUpdateCost(current, t, current.finalCost + V_H_COST);
			}

			if (current.j + 1 < grid[0].length) {
				t = grid[current.i][current.j + 1];
				checkAndUpdateCost(current, t, current.finalCost + V_H_COST);
			}

			if (current.i + 1 < grid.length) {
				t = grid[current.i + 1][current.j];
				checkAndUpdateCost(current, t, current.finalCost + V_H_COST);

				if (current.j - 1 >= 0) {
					t = grid[current.i + 1][current.j - 1];
					checkAndUpdateCost(current, t, current.finalCost + DIAGONAL_COST);
				}

				if (current.j + 1 < grid[0].length) {
					t = grid[current.i + 1][current.j + 1];
					checkAndUpdateCost(current, t, current.finalCost + DIAGONAL_COST);
				}
			}
		}
	}

	public static void test(int tCase, int x, int y, int si, int sj, int ei, int ej, int[][] blocked) {
		System.out.println("\n\n테스트! #" + tCase);
		grid = new Cell[x][y];
		closed = new boolean[x][y];
		open = new PriorityQueue<>((Object o1, Object o2) -> {
			Cell c1 = (Cell) o1;
			Cell c2 = (Cell) o2;

			return c1.finalCost < c2.finalCost ? -1 : c1.finalCost > c2.finalCost ? 1 : 0;
		});

		setStartCell(si, sj);

		setEndCell(ei, ej);

		for (int i = 0; i < x; ++i) {
			for (int j = 0; j < y; ++j) {
				grid[i][j] = new Cell(i, j);
				grid[i][j].heuristicCost = Math.abs(i - endI) + Math.abs(j - endJ);

			}

		}
		grid[si][sj].finalCost = 0;

		for (int i = 0; i < blocked.length; ++i) {
			setBlocked(blocked[i][0], blocked[i][1]);
		}

		System.out.println("Grid: ");
		for (int i = 0; i < x; ++i) {
			for (int j = 0; j < y; ++j) {
				if (i == si && j == sj)
					System.out.print("SO  ");
				else if (i == ei && j == ej)
					System.out.print("DE  ");
				else if (grid[i][j] != null)
					System.out.printf("%-3d ", 0);
				else
					System.out.print("BL  ");
			}
			System.out.println();
		}
		System.out.println();

		AStar();
		System.out.println("\n비용: ");
		for (int i = 0; i < x; ++i) {
			for (int j = 0; j < x; ++j) {
				if (grid[i][j] != null)
					System.out.printf("%-3d ", grid[i][j].finalCost);
				else
					System.out.print("BL  ");
			}
			System.out.println();
		}
		System.out.println();

		if (closed[endI][endJ]) {
			System.out.println("Path: ");
			Cell current = grid[endI][endJ];
			System.out.print(current);
			Mposition[endI][endJ].setVisible(true);
			Mposition[endI][endJ].setBackground(Color.green);
			while (current.parent != null) {
				Mposition[current.parent.i][current.parent.j].setVisible(true);
				Mposition[current.parent.i][current.parent.j].setBackground(Color.blue);
				System.out.print(" -> " + current.parent);
				current = current.parent;
			}
			System.out.println();
		} else
			System.out.println("길이 없습니다.");
	}

	public static void main(String[] args) {

		block = new int[817][2];
		int y = 51;
		int x = 56;
		int ycount = 0;
		int xcount = 0;
		for (int i = 0; i < 7; i++) {
			for (int j = 0; j < 12; j++) {
				block[ycount][0] = y;
				ycount++;
			}
			y++;
		}
		ycount = 0;
		for (int i = 0; i < 7; i++) {
			for (int j = 0; j < 12; j++) {
				block[ycount][1] = x;
				ycount++;
				x++;
			}
			x = 56;
		}

		int naky = 51;
		int nakx = 71;
		ycount = 84;
		for (int i = 0; i < 14; i++) {
			for (int j = 0; j < 10; j++) {
				block[ycount][0] = naky;
				ycount++;
			}
			naky++;
		}
		ycount = 84;
		for (int i = 0; i < 14; i++) {
			for (int j = 0; j < 10; j++) {
				block[ycount][1] = nakx;
				ycount++;
				nakx++;
			}
			nakx = 71;
		}

		int tamy = 9;
		int tamx = 13;
		ycount = 224;
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 13; j++) {
				block[ycount][0] = tamy;
				ycount++;
			}
			tamy++;
		}
		ycount = 224;
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 13; j++) {
				block[ycount][1] = tamx;
				ycount++;
				tamx++;
			}
			tamx = 13;
		}

		int jiny = 23;
		int jinx = 21;
		ycount = 289;
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 19; j++) {
				block[ycount][0] = jiny;
				ycount++;
			}
			jiny++;
		}
		ycount = 289;
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 19; j++) {
				block[ycount][1] = jinx;
				ycount++;
				jinx++;
			}
			jinx = 21;
		}

		int uy = 24;
		int ux = 46;
		ycount = 384;
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 24; j++) {
				block[ycount][0] = uy;
				ycount++;
			}
			uy++;
		}
		ycount = 384;
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 24; j++) {
				block[ycount][1] = ux;
				ycount++;
				ux++;
			}
			ux = 46;
		}

		int miy = 37;
		int mix = 52;
		ycount = 456;
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 17; j++) {
				block[ycount][0] = miy;
				ycount++;
			}
			miy++;
		}
		ycount = 456;
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 17; j++) {
				block[ycount][1] = mix;
				ycount++;
				mix++;
			}
			mix = 52;
		}

		int yoeny = 47;
		int yoenx = 26;
		ycount = 592;
		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 20; j++) {
				block[ycount][0] = yoeny;
				ycount++;
			}
			yoeny++;
		}
		ycount = 592;
		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 20; j++) {
				block[ycount][1] = yoenx;
				ycount++;
				yoenx++;
			}
			yoenx = 26;
		}

		int jiy = 57;
		int jix = 28;
		ycount = 712;
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 15; j++) {
				block[ycount][0] = jiy;
				ycount++;
			}
			jiy++;
		}
		ycount = 712;
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 15; j++) {
				block[ycount][1] = jix;
				ycount++;
				jix++;
			}
			jix = 28;
		}

		int gongy = 64;
		int gongx = 31;
		ycount = 772;
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 15; j++) {
				block[ycount][0] = gongy;
				ycount++;
			}
			gongy++;
		}
		ycount = 772;
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 15; j++) {
				block[ycount][1] = gongx;
				ycount++;
				gongx++;
			}
			gongx = 31;
		}

		// Block 처리

		for (int i = 0; i < 90; i++) {
			lat[i] = latitude;
			latitude -= 0.000030;
		}

		for (int i = 0; i < 100; i++) {

			lon[i] = longitude;
			longitude += 0.000040;
		}

		gps GUI = new gps();

		GUI.GPSframe();
	}
}