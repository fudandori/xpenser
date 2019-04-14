package application.process;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import javafx.concurrent.Task;

public class Processor extends Task<Map<String, Float>> {

	private static final int BALANCE_COLUMN = 5;
	private static final int CONCEPT_COLUMN = 2;
	private static final int EXPENSES_COLUMN = 3;
	private File file;

	public Processor(File file) {
		this.file = file;
	}

	@Override
	protected Map<String, Float> call() throws Exception {
		Map<String, Float> map = new HashMap<>();
		try {
			updateProgress(1,100);

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

					/*
					 * DateTimeFormatter formatter =
					 * DateTimeFormatter.ofPattern("yyyy-MM-dd"); LocalDate dt =
					 * LocalDate.parse(r.getCell(0).getStringCellValue(),
					 * formatter);
					 *
					 * String date = dt.toString();
					 */

					String name = r.getCell(CONCEPT_COLUMN).getStringCellValue();

					Float parsed = getNumericValue(r.getCell(EXPENSES_COLUMN));
					Float value = map.get(name);

					value = value == null ? parsed : parsed + value;
					map.put(name, round(value, 2).floatValue());

					updateProgress(current, total);
				}
				current++;
			}
		} catch (EncryptedDocumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}


		return map;
	}

	private static BigDecimal round(float d, int decimalPlace) {
		BigDecimal bd = new BigDecimal(Float.toString(d));
		bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
		return bd;
	}

	private static String getStringValue(Cell cell) {
		String result = "";

		CellType type = cell.getCellType();


		if(type == CellType.NUMERIC) {
			result = Double.toString(cell.getNumericCellValue());
		} else if (type == CellType.STRING) {
			result = cell.getStringCellValue();
		}

		return result;
	}
	
	private static Float getNumericValue(Cell cell) {
		Float result = null;
		
		if(cell.getCellType() == CellType.NUMERIC) {
			result = (float) cell.getNumericCellValue();
		} else if (cell.getCellType() == CellType.STRING) {
			result = Float.parseFloat(cell.getStringCellValue().replaceAll(",", ""));
		}
		
		return result;
	}

}
