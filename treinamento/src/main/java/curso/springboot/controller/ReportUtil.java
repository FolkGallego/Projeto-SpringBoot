package curso.springboot.controller;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletContext;

import org.springframework.stereotype.Component;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Component
public class ReportUtil implements Serializable{

	
	private static final long serialVersionUID = 1L;

	public byte[] gerarRelatorio (List listDados, String relatorio, ServletContext servletContext)
	throws Exception{	
	
		//1- Cria a lista de Dados do Relatório com a Lista de Objetos para Imprimir 
		JRBeanCollectionDataSource jrbcds = new JRBeanCollectionDataSource(listDados);
	
		//2- Carrega o Caminho do Arquivo Jasper Compilado
		String caminhoJasper = servletContext.getRealPath("relatorios") + File.separator + relatorio + ".jasper";
		
		//3- Carrega o Arquivo Jasper passando os Dados
		JasperPrint impressoraJasper = JasperFillManager.fillReport(caminhoJasper, new HashMap(), jrbcds);
	
		//4- Exporta para Byte[] para fazer dowload em PDF
		return JasperExportManager.exportReportToPdf(impressoraJasper);
	}
	
	//Oque acontece aqui é quem passamos qualquer Lista de Objetos, mas essa lista já vem filtrada, aqui apenas direcionamos o Relatório que será impresso
	
}
