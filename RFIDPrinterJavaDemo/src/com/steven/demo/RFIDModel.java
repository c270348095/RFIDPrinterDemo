package com.steven.demo;

public class RFIDModel {

	public static void main(String[] args) {
		
		model(new RFIDPrinter(),"Zebra ZD500R (203 dpi)");
	}
	
	/**
	 * ģ���ӡ
	 * @param printer ��ӡ��ʵ��RFIDPrinter
	 * @param printerURI �豸�ʹ�ӡ���µĴ�ӡ��ȫ��
	 */
	public static void model(RFIDPrinter printer,String printerURI){
		//��ӡ����ʼ��
		printer.init(printerURI);
		//���ñ�ǩֽ������
		printer.setTag(160, 583, 25, 2, 10, 0);
		
		//������ʽģ�� �������룩
        String barZpl = "^FO24,50^BY4,2.0,80^B3N,N,80,N,N^FD${data}^FS"; 
        //�����·����� 
        String bar = "steven";
        printer.setChar(bar, 120, 140, 32, 40);
        printer.setBarcode(bar, barZpl);
        //�����ı���Ϣ
        printer.setText("���������", 300, 128, 32, 32, 24, 2, 2, 20);
        //Ƕ��RFID����
        printer.setRFID("adbef20181130");
        //���ô�ӡ����
        printer.printCount(1);
        //��ӡ
        boolean result = printer.print(printer.getZpl());  
        System.out.println(printer.getZpl()); 
	}
}
