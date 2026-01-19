package com.tms.restapi.toolsmanagement.excel.service;

import com.tms.restapi.toolsmanagement.excel.dto.ExcelResponse;
import com.tms.restapi.toolsmanagement.tools.model.Tool;
import com.tms.restapi.toolsmanagement.tools.repository.ToolRepository;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ToolExcelService {

    @Autowired
    private ToolRepository toolRepository;

    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public ExcelResponse uploadTools(MultipartFile file) {

        int total = 0;
        int success = 0;
        int failed = 0;
        int duplicate = 0;

        try {

            InputStream is = file.getInputStream();
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheetAt(0);

            List<Tool> toolList = new ArrayList<>();

            for (int i = 4; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                total++;

                try {

                    Tool tool = new Tool();

                    String location = getString(row.getCell(0));
                    String siNo = getString(row.getCell(1));

                    // new duplicate check based on si_no and location
                    if (toolRepository.existsBySiNoAndLocation(siNo, location)) {
                        duplicate++;
                        continue;
                    }

                    tool.setSiNo(siNo);
                    tool.setLocation(location);

                    tool.setToolNo(getString(row.getCell(2)));

                    tool.setDescription(getString(row.getCell(3)));

                    tool.setToolLocation(getString(row.getCell(4)));

                    int quantity = (int) row.getCell(5).getNumericCellValue();

                    tool.setQuantity(quantity);

                    tool.setAvailability(quantity);

                    tool.setCondition(getCondition(row));

                    String calReq = getString(row.getCell(10));

                    if (calReq.equalsIgnoreCase("NA")) {

                        tool.setCalibrationRequired(false);
                        tool.setCalibrationPeriodMonths(null);
                        tool.setNextCalibrationDate(null);

                    } else {

                        tool.setCalibrationRequired(true);

                        if (calReq.contains("12")) {
                            tool.setCalibrationPeriodMonths(12);
                        }

                        if (calReq.contains("24")) {
                            tool.setCalibrationPeriodMonths(24);
                        }

                        String dateStr = getString(row.getCell(11));

                        LocalDate lastDate =
                                LocalDate.parse(dateStr, formatter);

                        tool.setLastCalibrationDate(lastDate);

                        tool.setNextCalibrationDate(
                                lastDate.plusMonths(
                                        tool.getCalibrationPeriodMonths()
                                )
                        );
                    }

                    tool.setRemark(getString(row.getCell(12)));

                    tool.setCreatedBy("System");
                    tool.setLastBorrowedBy(null);
                    tool.setCreatedAt(LocalDateTime.now());

                    toolList.add(tool);

                    success++;

                } catch (Exception e) {
                    failed++;
                }
            }

            toolRepository.saveAll(toolList);

            workbook.close();

            return new ExcelResponse(
                    total, success, failed, duplicate,
                    "Excel uploaded successfully"
            );

        } catch (Exception e) {

            return new ExcelResponse(
                    0, 0, 0,0,
                    "Error while processing file"
            );
        }
    }

    private String getString(Cell cell) {

        if (cell == null) return "";

        if (cell.getCellType() == CellType.NUMERIC) {
            long value = (long) cell.getNumericCellValue();
            return String.valueOf(value);
        }

        return cell.getStringCellValue().trim();
    }

    private String getCondition(Row row) {

        if (row.getCell(6).getNumericCellValue() == 1) {
            return "GOOD";
        }

        if (row.getCell(7).getNumericCellValue() == 1) {
            return "DAMAGED";
        }

        if (row.getCell(8).getNumericCellValue() == 1) {
            return "MISSING";
        }

        if (row.getCell(9).getNumericCellValue() == 1) {
            return "OBSOLETE";
        }

        return "GOOD";
    }
}
