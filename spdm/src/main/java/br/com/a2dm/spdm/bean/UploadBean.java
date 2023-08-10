package br.com.a2dm.spdm.bean;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import br.com.a2dm.brcmn.entity.Parametro;
import br.com.a2dm.brcmn.service.ParametroService;
import br.com.a2dm.brcmn.util.jsf.AbstractBean;
import br.com.a2dm.spdm.entity.NaoConformidade;
import br.com.a2dm.spdm.service.NaoConformidadeService;

@SessionScoped
@ManagedBean
public class UploadBean extends AbstractBean<NaoConformidade, NaoConformidadeService>
{
	private Part file;
	private NaoConformidade naoConformidade;
	private String path;
	
	public UploadBean()
	{
		super(NaoConformidadeService.getInstancia());
		this.ACTION_SEARCH = "upload";
		this.pageTitle = "Upload de Não Conformidade";
		this.path = "https://specialepaes.com/wp-content/uploads/2016/12/speciale-paes-pao-bananacomcanela-1.png";
	}
		
	public String preparaUpload()
	{
		try
		{
			this.setCurrentState(STATE_EDIT);
			
			NaoConformidade naoConformidade = new NaoConformidade();
			naoConformidade.setIdNaoConformidade(naoConformidade.getIdNaoConformidade());
			naoConformidade = NaoConformidadeService.getInstancia().get(naoConformidade, 0);
			
			this.setNaoConformidade(naoConformidade);
						
			if(naoConformidade != null) {
				Parametro parametro = new Parametro();
				parametro.setDescricao("PATH_LEITURA_IMG_NAO_CONFORMIDADE");
				parametro = ParametroService.getInstancia().get(parametro, 0);
				
				this.path = parametro.getValor() + "NC" + naoConformidade.getIdNaoConformidade() + ".jpg";
			}
			
			
		}
		catch (Exception e)
		{
			FacesMessage message = new FacesMessage(e.getMessage());
			message.setSeverity(FacesMessage.SEVERITY_ERROR);
			if(e.getMessage() == null)
				FacesContext.getCurrentInstance().addMessage("", message);
			else
				FacesContext.getCurrentInstance().addMessage(null, message);
		}
		
		return ACTION_SEARCH;
	}
	
	public void validarArquivo(Part value) throws Exception
	{
        Part arquivo = (Part) value;

        if (arquivo.getSize() > (2*1024*1024))
        {
            throw new Exception("Arquivo muito grande. O arquivo deve ter, no máximo, 2mb.");
        }

        if (!"image/jpg".equals(arquivo.getContentType())
        		&& !"image/jpeg".equals(arquivo.getContentType()))
        {
            throw new Exception("Tipo de arquivo inválido. O arquivo deve ser dos tipos: .jpg ou .jpeg");
        }
    }
	
	private String getFileName(Part part)
	{
		for (String cd : part.getHeader("content-disposition").split(";"))
		{
			if (cd.trim().startsWith("filename"))
			{
				return cd.substring(cd.indexOf('=') + 1).trim()
						.replace("\"", "");
			}
		}
		return null;
	}
	
	@SuppressWarnings("resource")
	public String salvar()
	{
		try
		{	
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			this.setFile(request.getPart("file"));
			
			if(this.getFileName(this.getFile()) == null
					|| this.getFileName(this.getFile()).equals(""))
			{
				throw new Exception("Selecione um arquivo para alterar a imagem.");
			}
			
			//new Scanner(file.getInputStream()).useDelimiter("\\A").next();
			
			this.validarArquivo(file);			
			this.getNaoConformidade().setFile(file);
			                        
            NaoConformidadeService.getInstancia().salvarImagem(this.getNaoConformidade());
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Imagem atualizada com sucesso.", null));
        } 
		catch (Exception e)
		{
        	FacesMessage message = new FacesMessage(e.getMessage());
	        message.setSeverity(FacesMessage.SEVERITY_ERROR);
	        FacesContext.getCurrentInstance().addMessage(null, message);
        }
		
		return ACTION_SEARCH;
	}

	public Part getFile() {
		return file;
	}

	public void setFile(Part file) {
		this.file = file;
	}

	public NaoConformidade getNaoConformidade() {
		return naoConformidade;
	}

	public void setNaoConformidade(NaoConformidade naoConformidade) {
		this.naoConformidade = naoConformidade;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
}