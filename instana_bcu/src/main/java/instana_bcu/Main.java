package instana_bcu;

import java.text.SimpleDateFormat;

public class Main {

	public static void main(String args[]) {
		try {
			
			System.out.println("usage java -jar instana_bcu.jar [METRIC] 2021-04-05");
			System.out.println(" [METRIC] should be the one of CPU | MEM | APP ");

			String startDate = "2021-04-05 00:00:00";
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
			String metric = "";
			if(args.length != 2) {
				System.err.println("please check usage again");
				System.exit(0);
			}else {
				metric = args[0];
				String startDateArg = args[1];
				startDate = startDateArg + " 00:00:00";
			}
			
			int interval =  1000 * 60 * 60 * 24;			
			long fromDate = format.parse(startDate).getTime();

			if(metric.equals("CPU")) {
				new InstanaMetricApi().cpuMetric(fromDate, interval);
			}else if(metric.equals("MEM") ) {
				new InstanaMetricApi().memMetric(fromDate, interval);
			}else if(metric.equals("APP")) {
				new InstanaMetricApi().appMetric(fromDate, interval);
			}
			fromDate += interval;
			
		}catch(Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}
}
