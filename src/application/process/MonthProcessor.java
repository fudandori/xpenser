package application.process;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import javafx.concurrent.Task;

public class MonthProcessor extends Task<Map<String, Map<String, Float>>> {

	private static final int BALANCE_COLUMN = 5;
	private static final int CONCEPT_COLUMN = 2;
	private static final int EXPENSES_COLUMN = 3;
	private static final int DATE_COLUMN = 0;
	private File file;

	public MonthProcessor(File file) {
		this.file = file;
	}

	@Override
	protected Map<String, Map<String, Float>> call() throws Exception {
		Map<String, Map<String, Float>> result = new HashMap<>();
		Map<String, Float> map;
		
		try {
			updateProgress(1, 100);

			Workbook wb = WorkbookFactory.create(file);
			Sheet s = wb.getSheetAt(0);

			String balance = null;
			String lastBalance = null;

			Iterator<Row> i = s.rowIterator();

			double total = s.getPhysicalNumberOfRows();
			int current = 1;

			while (i.hasNext()) {
				Row r = i.next();

				Cell balanceCell = r.getCell(BALANCE_COLUMN);
				balance = getStringValue(balanceCell);

				if (r.getRowNum() > 0 && !balance.equals(lastBalance)) {

					lastBalance = balance;

					DateTimeFormatter f = DateTimeFormatter.ofPattern("MMMM - yyyy");
					String st = getCellValue(r.getCell(DATE_COLUMN));
					st = st.substring(0, 7) + "-01";
					
					if(!result.containsKey(st)) {
						result.put(st, new HashMap<>());
					}
					
					map = result.get(st);
					
					String name = r.getCell(CONCEPT_COLUMN).getStringCellValue();

					Float parsed = Float.parseFloat(getCellValue(r.getCell(EXPENSES_COLUMN)).replaceAll(",", ""));
					Float value = map.get(name);

					value = value == null ? parsed : parsed + value;
					map.put(name, round(value, 2).floatValue());

					updateProgress(current, total);
				}
				current++;
			}
		} catch (EncryptedDocumentException | IOException e) {
			e.printStackTrace();
		}
		
		for (Entry<String, Map<String, Float>> m : result.entrySet()) {
			for(Entry<String, Float> i : m.getValue().entrySet()) {
				System.out.println(m.getKey() + " - " + i.getKey() + " -> " + i.getValue());
			}
		}

		return result;
	}

	private static BigDecimal round(float d, int decimalPlace) {
		BigDecimal bd = new BigDecimal(Float.toString(d));
		bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
		return bd;
	}

	private static String getStringValue(Cell cell) {
		String result = "";

		CellType type = cell.getCellType();

		if (type == CellType.NUMERIC) {
			result = Double.toString(cell.getNumericCellValue());
		} else if (type == CellType.STRING) {
			result = cell.getStringCellValue();
		}

		return result;
	}

	private static String getCellValue(Cell cell) {
		String result = null;

		switch (cell.getCellType()) {
			case NUMERIC:
				if (DateUtil.isCellDateFormatted(cell)) {
					result = DateUtil
							.getJavaDate(cell.getNumericCellValue())
							.toInstant()
							.atZone(ZoneId.systemDefault())
							.toLocalDate().toString();
				} else {
					result = Double.toString(cell.getNumericCellValue());
				}
				
				break;
				
			case STRING:
				result = cell.getStringCellValue();
				break;
				
			default:
				break;
		}

		return result;
	}
}
