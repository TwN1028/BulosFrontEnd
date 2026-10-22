import pandas as pd

excel_path = "app/src/main/assets/Dumagat_Dataset_2026-10-22_03-02.xlsx"
xls = pd.ExcelFile(excel_path)

for sheet_name in xls.sheet_names:
    if "GRAMMAR" in sheet_name.upper() or "RULE" in sheet_name.upper() or "EVIDENCE" in sheet_name.upper():
        print(f"=== SHEET: {sheet_name} ===")
        df = pd.read_excel(xls, sheet_name=sheet_name)
        print(df.to_string())
        print("\n" + "="*40 + "\n")
