package br.com.a2dm.spdm.bean;

import java.math.BigInteger;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.Part;

import br.com.a2dm.brcmn.util.jsf.AbstractBean;
import br.com.a2dm.brcmn.util.jsf.Variaveis;
import br.com.a2dm.spdm.config.MenuControl;
import br.com.a2dm.spdm.entity.NaoConformidade;
import br.com.a2dm.spdm.service.NaoConformidadeService;
import br.com.a2dm.spdm.utils.UploadHelper;

@SessionScoped
@ManagedBean
public class UploadBean extends AbstractBean<NaoConformidade, NaoConformidadeService> {
	private String image;
	private Part part;
	private BigInteger idNaoConformidade;

	public UploadBean() {
		super(NaoConformidadeService.getInstancia());

		this.ACTION_SEARCH = "upload";
		this.pageTitle = "Não Conformidade";

		MenuControl.ativarMenu("flgMenuMan");
		MenuControl.ativarSubMenu("flgMenuManCli");
	}

	public void upload() {
		try {
			UploadHelper uploadHelper = new UploadHelper();
			String newImage = uploadHelper.processUpload(this.part, this.getIdNaoConformidade());
			this.setImage(newImage);

			NaoConformidade nc = new NaoConformidade();
			nc.setIdNaoConformidade(this.getIdNaoConformidade());
			nc.setFoto(newImage);

			NaoConformidadeService.getInstancia().atualizarFoto(nc);
		} catch (Exception e) {
			FacesMessage message = new FacesMessage(e.getMessage());
			message.setSeverity(FacesMessage.SEVERITY_ERROR);
			if (e.getMessage() == null)
				FacesContext.getCurrentInstance().addMessage("", message);
			else
				FacesContext.getCurrentInstance().addMessage(null, message);
		}
	}

	public void print() {
		System.out.println(this.getIdNaoConformidade());
	}

	public void ativar() {
		try {
			if (this.getEntity() != null) {
				if (validarAcesso(Variaveis.ACAO_ATIVAR)) {
					NaoConformidadeService.getInstancia().atualizarFoto(getEntity());

					FacesMessage message = new FacesMessage("Registro ativado com sucesso!");
					message.setSeverity(FacesMessage.SEVERITY_INFO);
					FacesContext.getCurrentInstance().addMessage(null, message);
				}
			}
		} catch (Exception e) {
			FacesMessage message = new FacesMessage(e.getMessage());
			message.setSeverity(FacesMessage.SEVERITY_ERROR);
			FacesContext.getCurrentInstance().addMessage(null, message);
		}
	}

	public String cancelar() {
		return "naoConformidade";
	}

	public String preparaUpload(BigInteger idNaoConformidade) {
		this.setIdNaoConformidade(idNaoConformidade);
		return "upload";
	}

	public Part getPart() {
		return part;
	}

	public void setPart(Part part) {
		this.part = part;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public BigInteger getIdNaoConformidade() {
		return idNaoConformidade;
	}

	public void setIdNaoConformidade(BigInteger idNaoConformidade) {
		this.idNaoConformidade = idNaoConformidade;
	}

}