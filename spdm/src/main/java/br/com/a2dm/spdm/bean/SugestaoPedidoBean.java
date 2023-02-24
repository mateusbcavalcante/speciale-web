package br.com.a2dm.spdm.bean;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

import br.com.a2dm.brcmn.entity.ativmob.Event;
import br.com.a2dm.brcmn.entity.ativmob.Form;
import br.com.a2dm.brcmn.util.jsf.AbstractBean;
import br.com.a2dm.spdm.config.MenuControl;
import br.com.a2dm.spdm.service.EventService;
import br.com.a2dm.spdm.service.FormService;

@RequestScoped
@ManagedBean
public class SugestaoPedidoBean extends AbstractBean<Event, EventService>
{
	public SugestaoPedidoBean()
	{
		super(EventService.getInstancia());
		this.ACTION_SEARCH = "sugestaoPedido";
		this.pageTitle = "Sugestão de Pedido";
		
		MenuControl.ativarMenu("flgMenuMan");
		MenuControl.ativarSubMenu("flgMenuManCli");
	}
	
	public void visualizar()
	{
		try
		{
			Form form = new Form();
			form.setIdEvent(this.getEntity().getId_event());
			
			List<Form> forms = FormService.getInstancia().pesquisar(form, 0);
			
			if (forms != null && forms.size() > 0) {
				Optional<Form> formImagemOptional = this.getObjectImage(forms);
				forms = this.getForms(forms);				
				setEntity(new Event(formImagemOptional.isPresent() ? formImagemOptional.get().getUrl() : null, forms));
			} else {
				setEntity(new Event());
			}
		}
		catch (Exception e) 
		{
			FacesMessage message = new FacesMessage(e.getMessage());
	        message.setSeverity(FacesMessage.SEVERITY_ERROR);
	        FacesContext.getCurrentInstance().addMessage(null, message);
		}
	}

	private List<Form> getForms(List<Form> forms) {
		return forms.stream().filter(x -> x.getValue() != null && x.getValue().intValue() > 0)
				             .collect(Collectors.toList());
	}

	private Optional<Form> getObjectImage(List<Form> forms) {
		return forms.stream().filter(x -> x.getLabel().equalsIgnoreCase(FormService.OBJECT_CAPTURE_IMAGE))
							 .findFirst();
	}
}