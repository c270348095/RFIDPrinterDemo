package com.steven.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.standard.PrinterName;

public class RFIDPrinter {

	//��ӡ������·��  
	private String printerURI = null;
	//��ӡ������  
    private PrintService printService = null;
    //��ǩ��ʽ��^XA��ʼ
    private String begin = "^XA ^JMA^LL160^PW583^MD30^PR2^PON^LRN^LH10,0";     
    //��ǩ��ʽ��^XZ����
    private String end = "^XZ"; 
    //��ǩ����
    private String content = "";
    private byte[] dotFont;  
    
    /**
     * ����RFIDǶ������
     * @param str ��ҪǶ��RFID������
     */
    public void setRFID(String str){
    	//����16����
    	content += "^RS8^RFW,H^FD"+str+"^FS";
    	
    	//����EPC
    	//content += "^RB64,2,20,18,24^RFw,E^FD01,02,03,0F^FS";
    }
    
    /** 
     * ���������� 
     * @param barcode �����ַ� 
     * @param zpl ������ʽģ�� 
     */  
    public void setBarcode(String barcode,String zpl) {  
        content += zpl.replace("${data}", barcode);  
    }
    
    /** 
     * �����ַ���Ӣ���ַ�(��������)��� 
     * @param str ���ġ�Ӣ�� 
     * @param x x���� 
     * @param y y���� 
     * @param eh Ӣ������߶�height 
     * @param ew Ӣ��������width 
     * @param es Ӣ��������spacing 
     * @param mx ����x������ͼ�ηŴ��ʡ���Χ1-10��Ĭ��1 
     * @param my ����y������ͼ�ηŴ��ʡ���Χ1-10��Ĭ��1 
     * @param ms ���������ࡣ24�Ǹ��ȽϺ��ʵ�ֵ�� 
     */  
    public void setText(String str, int x, int y, int eh, int ew, int es, int mx, int my, int ms) {  
        byte[] ch = str2bytes(str);  
        for (int off = 0; off < ch.length;) { 
        	//�����ַ�
            if (((int) ch[off] & 0x00ff) >= 0xA0) {  
                int qcode = ch[off] & 0xff;  
                int wcode = ch[off + 1] & 0xff;  
                content += String.format("^FO%d,%d^XG0000%01X%01X,%d,%d^FS\n", x, y, qcode, wcode, mx, my);  
                begin += String.format("~DG0000%02X%02X,00072,003,\n", qcode, wcode);  
                qcode = (qcode + 128 - 32) & 0x00ff;  
                wcode = (wcode + 128 - 32) & 0x00ff;  
                int offset = ((int) qcode - 16) * 94 * 72 + ((int) wcode - 1) * 72;  
                for (int j = 0; j < 72; j += 3) {  
                    qcode = (int) dotFont[j + offset] & 0x00ff;  
                    wcode = (int) dotFont[j + offset + 1] & 0x00ff;  
                    int qcode1 = (int) dotFont[j + offset + 2] & 0x00ff;  
                    begin += String.format("%02X%02X%02X\n", qcode, wcode, qcode1);  
                }  
                x = x + ms * mx;  
                off = off + 2;  
            } else if (((int) ch[off] & 0x00FF) < 0xA0) {  
                setChar(String.format("%c", ch[off]), x, y, eh, ew);  
                x = x + es;  
                off++;  
            }  
        }  
    }
    
    /** 
     * Ӣ���ַ���(��������) 
     * @param str Ӣ���ַ��� 
     * @param x x���� ����λ��Ϊ��dpi��
     * @param y y���� 
     * @param h �߶� 
     * @param w ��� 
     */  
    public void setChar(String str, int x, int y, int h, int w) {  
        content += "^FO" + x + "," + y + "^A0," + h + "," + w + "^FD" + str + "^FS";  
    }  
    
    /** 
     * Ӣ���ַ�(��������)˳ʱ����ת90�� 
     * @param str Ӣ���ַ��� 
     * @param x x���� 
     * @param y y���� 
     * @param h �߶� 
     * @param w ��� 
     */  
    public void setCharR(String str, int x, int y, int h, int w) {  
        content += "^FO" + x + "," + y + "^A0R," + h + "," + w + "^FD" + str + "^FS";  
    } 
    
    /** 
     * ��ȡ������ZPL 
     * @return ��������������ģ��
     */  
    public String getZpl() {  
        return begin + content + end;  
    } 
    
    /** 
     * ����ZPLָ�����Ҫ��ӡ����ֽ��ʱ����Ҫ���� 
     */  
    public void resetZpl() {  
        //begin = "^XA ^JMA^LL160^PW583^MD30^PR2^PON^LRN^LH10,0";  
        setTag(160, 583, 25, 2, 10, 0);
        end = "^XZ";  
        content = "";  
    } 
    
    /**
     * ���ñ�ǩֽ������
     * @param ll	��ǩ���ȣ������൱�ڸ߶ȣ�,��λΪ������dpi��	,Ĭ��160
     * @param pw ��ǩ��ȣ����򣩣�Ĭ��583
     * @param md ��ӡŨ�ȣ�-30 �� 30�������ñ�ǩ�ϵ�ֵ���мӼ���Ĭ��30
     * @param v ��ӡ�ٶȣ�1~14����ʾ1~14Ӣ��/s��
     * @param x	��ǩ���x��
     * @param y ��ǩ���y��
     */
    public void setTag(int ll, int pw, int md, int v,int x, int y){
    	begin = "^XA ^JMA^LL"+ll;
    	begin += "^PW"+pw;
    	begin += "^MD"+md;
    	begin += "^PR"+v;
    	begin += "^PON^LRN";
    	begin += "^LH"+x+","+y;
    }
    
    /**
     * ��ʼ����ӡ��
     * @param printerURI ��ӡ����ַ
     */
    public void init(String printerURI){
    	//1�����������ļ�
    	File file = new File("font/ts24.lib");
    	if(file.exists()){
    		FileInputStream fis = null;
    		try{
    			fis = new FileInputStream(file);
    			dotFont = new byte[fis.available()]; 
    			fis.read(dotFont);
    			
    			fis.close();
    		}catch(Exception e){
    			e.printStackTrace();
    		}
    	}else{
    		System.out.println("������ļ������ڣ�");
    	}
    	
    	//2����ʼ����ӡ��
    	PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
    	for (PrintService printer : services) {
			if(printerURI.equals(printer.getName())){
				printService =  printer;
				break;
			}
		}
    	
    	if(printService == null){
    		System.out.println("δ�ҵ���ӡ��["+printerURI+"]");
    		//ѭ���г����п��ô�ӡ��
    		if(services != null && services.length > 0){
    			System.out.print("���õĴ�ӡ���б�[");
	    		for (int i = 0; i < services.length; i++) {
	    			if(i != services.length - 1){
	    				System.out.print(services[i].getName()+",");
					}else{
						System.out.print(services[i].getName()+"]");
					}
				}
    		}
    	}else{
    		System.out.println("�ҵ���ӡ����\t["+printerURI+":"+printService.getAttribute(PrinterName.class).getValue()+"]");
    	}
    	
    }
    
    /**
     * ��ӡ��ǩ
     * @param zpl ������ZPL����
     * @return ��ӡ�ɹ�״̬
     */
    public boolean print(String zpl){
    	if(printService == null){
    		System.out.println("��ӡ����δ�ҵ���ӡ��:["+printerURI+"]");
    		return false;
    	}
    	
    	DocPrintJob job = printService.createPrintJob();
        Doc doc = new SimpleDoc(zpl.getBytes(), DocFlavor.BYTE_ARRAY.AUTOSENSE, null);
        try {
			job.print(doc, null);
			System.out.println("�Ѵ�ӡ��");
			return true;
		} catch (PrintException e) {
			e.printStackTrace();
			return false;
		}
    }
    
    /**
     * ��ӡ��ǩ����
     * @param printCount ��ӡ����
     */
    public void printCount(int printCount){
    	content += "^PQ"+printCount+",0,1,Y";
    }
    
    /** 
     * �ַ���תbyte[] 
     * @param s ��ת�����ַ���
     * @return 
     */  
    private byte[] str2bytes(String str) {  
        if (null == str || "".equals(str)) {  
            return null;  
        }  
        
        byte[] result = null;  
        try {  
        	result = str.getBytes("gb2312");  
        } catch (UnsupportedEncodingException ex) {  
            ex.printStackTrace();  
        }  
        return result;  
    }
}
