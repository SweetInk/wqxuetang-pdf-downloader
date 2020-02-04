package online.githuboy.wqxuetang.pdfd.utils;

import cn.hutool.core.io.FileUtil;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.action.PdfAction;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.extgstate.PdfExtGState;
import com.itextpdf.kernel.pdf.navigation.PdfDestination;
import com.itextpdf.kernel.pdf.navigation.PdfExplicitDestination;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.VerticalAlignment;
import lombok.extern.slf4j.Slf4j;
import online.githuboy.wqxuetang.pdfd.pojo.BookMetaInfo;
import online.githuboy.wqxuetang.pdfd.pojo.Catalog;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * PDF生成工具
 *
 * @author suchu
 * @since 2020年2月3日
 */
@Slf4j
public class PDFUtils {

    private PDFUtils() {
    }

    public static void gen(BookMetaInfo bookMetaInfo, List<Catalog> catalogs, String workDir) throws IOException {
        String bookName = bookMetaInfo.getName();
        int pageNum = bookMetaInfo.getPages();
        File outFile = FileUtil.file(workDir, "pdfTest\\" + bookName + ".pdf");
        PdfWriter writer = new PdfWriter(FileUtil.touch(outFile));
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);
        boolean allInserted = true;
        //插入图片
        for (int i = 1; i <= pageNum; i++) {
            try {
                document.add(new Image(ImageDataFactory.create(new URL("file:///" + workDir + "\\" + bookMetaInfo.getBid() + "\\" + i + ".jpg"))));
            } catch (Exception e) {
                log.error("图片:{}损坏", i);
                e.printStackTrace();
                allInserted = false;
                break;
            }
        }
        if (!allInserted) {
            log.warn("生成PDF失败,删除损坏图片，重新运行程序");
            return;
        }
        PdfOutline root = pdf.getOutlines(false);
        //构建目录概述
        log.info("生成目录信息");
        for (Catalog child : catalogs) {
            buildOutline(root, child);
        }
        document.close();
        //Add watermark
        addWatermark(outFile);
        log.info("PDF file saved in：{}", outFile.getAbsolutePath());
    }

    private static void addWatermark(File file) throws IOException {
        PdfReader reader = new PdfReader(file);
        File tempOutFile = new File(file.getParent(), "out.pdf");
        PdfWriter writer = new PdfWriter(tempOutFile);
        PdfDocument pdf = new PdfDocument(reader, writer);
        Document document = new Document(pdf);
        PdfFont font = PdfFontFactory.createFont("c:\\windows\\fonts\\msyh.ttc,1", PdfEncodings.IDENTITY_H, true);
        int numberOfPages = pdf.getNumberOfPages();
        PdfExtGState gs1 = new PdfExtGState().setFillOpacity(0.2f);
        Paragraph paragraph = new Paragraph("试读样张,请支持正版").setFont(font).setFontSize(30);
        for (int i = 1; i <= numberOfPages; i++) {
            PdfPage pdfPage = pdf.getPage(i);
            Rectangle pageSize = pdfPage.getPageSize();
            float x = (pageSize.getLeft() + pageSize.getRight()) / 2;
            float y = (pageSize.getTop()) - 140;
            PdfCanvas over = new PdfCanvas(pdfPage);
            over.saveState();
            over.setExtGState(gs1);
            document.showTextAligned(paragraph, pageSize.getLeft(), y, i, TextAlignment.LEFT, VerticalAlignment.TOP, (float) (Math.PI / 6));
            over.restoreState();
        }
        String name = file.getName();
        document.close();
        file.delete();
        FileUtil.rename(tempOutFile, name, false, false);
    }

    /**
     * 递归生成pdf目录概述
     *
     * @param root    父级大纲节点
     * @param catalog 目录信息
     */
    private static void buildOutline(PdfOutline root, Catalog catalog) {
        int pageNumber = catalog.getPnum();
        String title = catalog.getLabel();
//        log.info("{} 页码:{}", title, pageNumber);
        PdfOutline current = root.addOutline(title);
        PdfDestination destination = PdfExplicitDestination.createFit(pageNumber);
        PdfAction action = PdfAction.createGoTo(destination);
        current.addAction(action);
        List<Catalog> children = catalog.getChildren();
        if (null != children && children.size() > 0) {
            for (Catalog child : children) {
                buildOutline(current, child);
            }
        }
    }


    private static void testWaterMark() throws IOException {
        PdfReader reader = new PdfReader(new File("D:\\Temp\\pdfTest\\计算机图形学.pdf"));
        PdfWriter writer = new PdfWriter(new File("D:\\Temp\\pdfTest\\计算机图形学_MK.pdf"));
        PdfDocument pdf = new PdfDocument(reader, writer);
        Document document = new Document(pdf);
        PdfFont font = PdfFontFactory.createFont("c:\\windows\\fonts\\msyh.ttc,1", PdfEncodings.IDENTITY_H, true);
        int numberOfPages = pdf.getNumberOfPages();
        PdfExtGState gs1 = new PdfExtGState().setFillOpacity(0.2f);
        Paragraph paragraph = new Paragraph("试读样张,请支持正版").setFont(font).setFontSize(30);
        for (int i = 1; i <= numberOfPages; i++) {
            PdfPage pdfPage = pdf.getPage(i);
            Rectangle pageSize = pdfPage.getPageSize();
            float x = (pageSize.getLeft() + pageSize.getRight()) / 2;
            float y = (pageSize.getTop()) - 140;
            PdfCanvas over = new PdfCanvas(pdfPage);
            over.saveState();
            over.setExtGState(gs1);
            document.showTextAligned(paragraph, pageSize.getLeft(), y, i, TextAlignment.LEFT, VerticalAlignment.TOP, (float) (Math.PI / 6));
            over.restoreState();
        }
        document.close();
    }

    public static void main(String[] args) throws IOException {
        testWaterMark();
    }
}
