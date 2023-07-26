package br.com.a2dm.spdm.bean;

import java.math.BigInteger;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.servlet.http.Part;

import br.com.a2dm.brcmn.util.jsf.AbstractBean;
import br.com.a2dm.spdm.config.MenuControl;
import br.com.a2dm.spdm.entity.NaoConformidade;
import br.com.a2dm.spdm.service.NaoConformidadeService;
import br.com.a2dm.spdm.utils.UploadHelper;

@RequestScoped
@ManagedBean
public class UploadBean extends AbstractBean<NaoConformidade, NaoConformidadeService>
{
	
	private String image = "";
	private Part part;
	
	public UploadBean()
	{
		super(NaoConformidadeService.getInstancia());

		this.ACTION_SEARCH = "naoConformidade";
		this.pageTitle = "Não Conformidade";
		
		MenuControl.ativarMenu("flgMenuMan");
		MenuControl.ativarSubMenu("flgMenuManCli");
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public Part getPart() {
		return part;
	}

	public void setPart(Part part) {
		this.part = part;
	}

	public String processUpload(BigInteger idImage) {
		System.out.println("=============================================");
		System.out.println("Iniciando processo de Upload");
		System.out.println("Part: " + part);
		System.out.println("image:" + image);
		UploadHelper uploadHelper = new UploadHelper();
		System.out.println("idImage: " + idImage);
		this.image = uploadHelper.processUpload(this.part, idImage);
		System.out.println("image:" + image);
		System.out.println("Finalizando processo de Upload");
		System.out.println("=============================================");
		return "result";
	}

	
}