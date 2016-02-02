/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.gob.ceaaces.habilitacion.controlador;

import ec.gob.ceaaces.catalogo.modelo.CantonIes;
import ec.gob.ceaaces.catalogo.modelo.Discapacidad;
import ec.gob.ceaaces.catalogo.modelo.Pais;
import ec.gob.ceaaces.catalogo.modelo.ParroquiaIes;
import ec.gob.ceaaces.catalogo.modelo.ProvinciaIes;
import ec.gob.ceaaces.catalogo.modelo.Pueblo;
import ec.gob.ceaaces.catalogo.modelo.Region;
import ec.gob.ceaaces.catalogo.modelo.enumeraciones.EtniaEnum;
import ec.gob.ceaaces.catalogo.modelo.enumeraciones.NivelDiscapacidadEnum;
import ec.gob.ceaaces.habilitacion.controlador.auth.LogoutControlador;
import ec.gob.ceaaces.habilitacion.modelo.DocumentoProcesoExamen;
import ec.gob.ceaaces.habilitacion.modelo.Inscripcion;
import ec.gob.ceaaces.habilitacion.modelo.ProcesoExamen;
import ec.gob.ceaaces.habilitacion.modelo.persona.Persona;
import ec.gob.ceaaces.habilitacion.proxy.ServicioCatalogoHabilitacion;
import ec.gob.ceaaces.habilitacion.proxy.ServicioHabilitacion;
import ec.gob.ceaaces.habilitacion.proxy.ServicioReporteDelegate;
import ec.gob.ceaaces.habilitacion.servicio.exception.ServicioHabilitacionException;
import ec.gob.ceaaces.habilitacion.util.JsfUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author mtoapanta
 */
@ManagedBean
@ViewScoped
public class DatosPostulacionControlador implements Serializable {

    private static final long serialVersionUID = -3028778697163884466L;

    private static final Logger LOG = Logger.getLogger(DatosPersonalesControlador.class.getName());

    private boolean nada = true;

    private List<EtniaEnum> listaEtnico;
    private List<Discapacidad> listaDiscapacidad;
    private List<NivelDiscapacidadEnum> listaNivelDiscapacidad;
    private Persona persona;
    private List<ProcesoExamen> listaProcesoExamen;
    private ProcesoExamen procesoExamenSeleccionado;
    private List<Inscripcion> listaInscripciones;
    private List<Region> listaRegiones;
    private List<Pueblo> listaPueblo;
    private List<ProvinciaIes> listaProvincia;
    private Map<String, String> parametros;
    private List<CantonIes> listaCantones;
    private List<ParroquiaIes> listaParroquias;
    private List<DocumentoProcesoExamen> listaDocumentoGenerales;
    private List<DocumentoProcesoExamen> listaDocumentoProceso;
    private DocumentoProcesoExamen documentoSeleccionado;
    private List<Pais> listaPaises;
    private Inscripcion inscripcionSeleccionada;
    private boolean discapacidad = false;
    private StreamedContent documentoDescarga;
    private StreamedContent documentosGeneralesDescarga;
    private StreamedContent descargarPostulacion;
    private StreamedContent descargarHojaExamen;
    private Date fechaMax;
    private boolean porcentajeDiscapacidad = false;
    private boolean datosPostulacion;
    private boolean documentosProcesos = false;
    private Long idRegion;
    private Long idPueblo;
    private Long idProceso;
    private boolean indigena = false;
    private String tipo_identificacion;

    @Inject
    private ServicioCatalogoHabilitacion servicioCatalogoHabilitacion;

    @Inject
    private LogoutControlador logoutControlador;

    @Inject
    private ServicioHabilitacion servicioHabilitacion;

    @Inject
    private ServicioReporteDelegate servicioReporte;

    public DatosPostulacionControlador() {
        persona = new Persona();
        listaProcesoExamen = new ArrayList<>();
        listaInscripciones = new ArrayList<>();
        listaEtnico = new ArrayList<>();
        listaDiscapacidad = new ArrayList<>();
        listaRegiones = new ArrayList<>();
        listaPueblo = new ArrayList<>();
        listaPaises = new ArrayList<>();
        listaProvincia = new ArrayList<>();
        listaDocumentoGenerales = new ArrayList<>();
        parametros = new HashMap<>();
        listaDiscapacidad = new ArrayList<>();
        listaNivelDiscapacidad = new ArrayList<>();
        listaParroquias = new ArrayList<>();
        listaCantones = new ArrayList<>();
        procesoExamenSeleccionado = new ProcesoExamen();
        inscripcionSeleccionada = new Inscripcion();
        documentoSeleccionado = new DocumentoProcesoExamen();

    }

    @PostConstruct
    private void cargarDatosIniciales() {

        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(true);
        persona = (Persona) session.getAttribute(ConstantesSesion.PERSONA);
        datosPostulacion = (boolean) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(ConstantesSesion.DATOS_POSTULACION);
        listaInscripciones = servicioHabilitacion.obtenerInscripcionesPorPersona(persona.getIdentificacion());
        listaDocumentoGenerales = servicioHabilitacion.obtenerDocumentosSinProceso();
        listaProcesoExamen = servicioHabilitacion.obtenerProcesoExamenTodos();
        if (persona == null) {
            cancelar();
        } else {
            listaEtnico.clear();
            listaDiscapacidad = servicioCatalogoHabilitacion.obtenerDiscapacidades();
            for (EtniaEnum etniaEnum : EtniaEnum.values()) {
                listaEtnico.add(etniaEnum);
            }
            cargarProvincia();
            cargarCanton();
            cargarParroquias();
            cargarPaises();
        }
    }

    public void controlNavegacion() {
        if (!datosPostulacion) {
            FacesContext context1 = FacesContext.getCurrentInstance();
            ConfigurableNavigationHandler handler = (ConfigurableNavigationHandler) context1
                    .getApplication().getNavigationHandler();
            handler.performNavigation("inicio?faces-redirect=true");
        }
    }

    public void cargarPaises() {
        listaPaises = servicioCatalogoHabilitacion.obtenerPaises();
    }

    public void cargarIndigena() {
        if (persona.getAutoidentificacionEtnica().equals("INDIGENA")) {
            listaRegiones = servicioCatalogoHabilitacion.obtenerRegiones();
            indigena = true;
        } else {
            indigena = false;
        }
    }

    public void cargarProvincia() {
        listaProvincia.clear();
        listaProvincia = servicioCatalogoHabilitacion.obtenerProvincia(1L);
    }

    public void cargarPueblo() {
        listaPueblo.clear();
        listaPueblo = servicioCatalogoHabilitacion.obtenerPueblo(persona.getPueblo().getIdRegion());

    }

    public void cargarCanton() {
        listaCantones.clear();
        if (null != persona.getUbicacionGeografica().getIdProvincia()) {
            listaCantones = servicioCatalogoHabilitacion.obtenerCantones(persona.getUbicacionGeografica().getIdProvincia());
        }
    }

    public void cargarParroquias() {
        listaParroquias.clear();
        if (null != persona.getUbicacionGeografica().getIdCanton()) {
            listaParroquias = servicioCatalogoHabilitacion.obtenerParroquiasPorCanton(persona.getUbicacionGeografica().getIdCanton());
        }
    }

    private boolean validarDatosPersonales() {
        if (persona.getUbicacionGeografica().getIdProvincia() == -99L) {
            JsfUtil.msgError("Debe seleccionar una provincia");
            return false;
        } else if (persona.getUbicacionGeografica().getIdCanton() == -99L) {
            JsfUtil.msgError("Debe seleccionar un canton");
            return false;
        } else {
            return true;
        }
    }

    public void mostrarDocumentosProcesos() {
        if (!idProceso.equals(-1L)) {
            listaDocumentoProceso = servicioHabilitacion.obtenerDocumentosProcesoExamen(idProceso,persona.getIdentificacion());
            documentosProcesos = true;
            for (ProcesoExamen proex : listaProcesoExamen) {
                if (idProceso.equals(proex.getFaseProceso().getProceso().getId())) {
                    procesoExamenSeleccionado = proex;
                    break;
                }
            }
        } else {
            documentosProcesos = false;

        }
    }

    public void guardarPersona(ActionEvent actionEvent) {
        try {
            servicioHabilitacion.registrarPersona(persona);
            JsfUtil.msgInfo("Datos Actualizados Satifactoriamente");
        } catch (ServicioHabilitacionException ex) {
            Logger.getLogger(DatosPostulacionControlador.class.getName()).log(Level.SEVERE, null, ex);
            JsfUtil.msgError(ex.getMessage());
        }

    }

    public String cancelar() {
        logoutControlador.logout();
        return "inicio?faces-redirect=true";
    }

    public String redirect() {
        if (!validarDatosPersonales()) {

        } else {
            persona.setFechaActualizacion(new Date());
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("persona", persona);
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ConstantesSesion.INSCRIPCION_POSTULANTE, true);
            return "inscripcionPostulante?faces-redirect=true";
        }
        return "";
    }

    public void cargarDiscapacidad() {
        if (persona.getDiscapacidad().equals("NINGUNA")) {
            discapacidad = false;
            porcentajeDiscapacidad = false;
            persona.setNivelDiscapacidad(null);

        } else {
            discapacidad = true;
            porcentajeDiscapacidad = false;
            persona.setNivelDiscapacidad(NivelDiscapacidadEnum.LEVE);
            listaNivelDiscapacidad.clear();
            for (NivelDiscapacidadEnum niveldis : NivelDiscapacidadEnum.values()) {
                listaNivelDiscapacidad.add(niveldis);
            }
        }

    }

    public void cargarPorcentaje() {
        if (persona.getNivelDiscapacidad().getValue().equals("DEFINIR PORCENTAJE")) {
            porcentajeDiscapacidad = true;
        } else {
            porcentajeDiscapacidad = false;
        }
    }

    public boolean isNada() {
        return nada;
    }

    public void setNada(boolean nada) {
        this.nada = nada;
    }

    public String getTipo_identificacion() {
        return tipo_identificacion;
    }

    public void setTipo_identificacion(String tipo_identificacion) {
        this.tipo_identificacion = tipo_identificacion;
    }

    public Persona getPersona() {
        return persona;
    }

    public void setPersona(Persona persona) {
        this.persona = persona;
    }

    public List<ProcesoExamen> getListaProcesoExamen() {
        return listaProcesoExamen;
    }

    public void setListaProcesoExamen(List<ProcesoExamen> listaProcesoExamen) {
        this.listaProcesoExamen = listaProcesoExamen;
    }

    public List<Inscripcion> getListaInscripciones() {
        return listaInscripciones;
    }

    public void setListaInscripciones(List<Inscripcion> listaInscripciones) {
        this.listaInscripciones = listaInscripciones;
    }

    public ProcesoExamen getProcesoExamenSeleccionado() {
        return procesoExamenSeleccionado;
    }

    public void setProcesoExamenSeleccionado(ProcesoExamen procesoExamenSeleccionado) {
        this.procesoExamenSeleccionado = procesoExamenSeleccionado;
    }

    public List<EtniaEnum> getListaEtnico() {
        return listaEtnico;
    }

    public void setListaEtnico(List<EtniaEnum> listaEtnico) {
        this.listaEtnico = listaEtnico;
    }

    public List<Region> getListaRegiones() {
        return listaRegiones;
    }

    public void setListaRegiones(List<Region> listaRegiones) {
        this.listaRegiones = listaRegiones;
    }

    public boolean isIndigena() {
        return indigena;
    }

    public void setIndigena(boolean indigena) {
        this.indigena = indigena;
    }

    public Long getIdRegion() {
        return idRegion;
    }

    public void setIdRegion(Long idRegion) {
        this.idRegion = idRegion;
    }

    public List<Pueblo> getListaPueblo() {
        return listaPueblo;
    }

    public void setListaPueblo(List<Pueblo> listaPueblo) {
        this.listaPueblo = listaPueblo;
    }

    public Long getIdPueblo() {
        return idPueblo;
    }

    public void setIdPueblo(Long idPueblo) {
        this.idPueblo = idPueblo;
    }

    public List<ProvinciaIes> getListaProvincia() {
        return listaProvincia;
    }

    public void setListaProvincia(List<ProvinciaIes> listaProvincia) {
        this.listaProvincia = listaProvincia;
    }

    public List<CantonIes> getListaCantones() {
        return listaCantones;
    }

    public void setListaCantones(List<CantonIes> listaCantones) {
        this.listaCantones = listaCantones;
    }

    public Long getIdProceso() {
        return idProceso;
    }

    public void setIdProceso(Long idProceso) {
        this.idProceso = idProceso;
    }

    public List<Discapacidad> getListaDiscapacidad() {
        return listaDiscapacidad;
    }

    public void setListaDiscapacidad(List<Discapacidad> listaDiscapacidad) {
        this.listaDiscapacidad = listaDiscapacidad;
    }

    public boolean isDiscapacidad() {
        return discapacidad;
    }

    public void setDiscapacidad(boolean discapacidad) {
        this.discapacidad = discapacidad;
    }

    public List<NivelDiscapacidadEnum> getListaNivelDiscapacidad() {
        return listaNivelDiscapacidad;
    }

    public void setListaNivelDiscapacidad(List<NivelDiscapacidadEnum> listaNivelDiscapacidad) {
        this.listaNivelDiscapacidad = listaNivelDiscapacidad;
    }

    public boolean isPorcentajeDiscapacidad() {
        return porcentajeDiscapacidad;
    }

    public void setPorcentajeDiscapacidad(boolean porcentajeDiscapacidad) {
        this.porcentajeDiscapacidad = porcentajeDiscapacidad;
    }

    public List<Pais> getListaPaises() {
        return listaPaises;
    }

    public void setListaPaises(List<Pais> listaPaises) {
        this.listaPaises = listaPaises;
    }

    public boolean isDatosPostulacion() {
        return datosPostulacion;
    }

    public void setDatosPostulacion(boolean datosPostulacion) {
        this.datosPostulacion = datosPostulacion;
    }

    public List<ParroquiaIes> getListaParroquias() {
        return listaParroquias;
    }

    public void setListaParroquias(List<ParroquiaIes> listaParroquias) {
        this.listaParroquias = listaParroquias;
    }

    public Date getFechaMax() {
        Calendar cal = Calendar.getInstance();
        int anio = Calendar.getInstance().get(Calendar.YEAR) - 18;
        cal.set(anio, Calendar.JANUARY, 1);
        fechaMax = new Date(cal.getTimeInMillis());
        return fechaMax;
    }

    public void setFechaMax(Date fechaMax) {
        this.fechaMax = fechaMax;
    }

    public StreamedContent getDocumentoDescarga() {
        InputStream is = null;
        try {
            parametros.put("ID_INSCRIPCION", String.valueOf(inscripcionSeleccionada.getId()));
            is = servicioReporte.getReporte(
                    "24", "/reports/habilitacion/Registro/RepResultadosInscripcion",
                    persona.getIdentificacion(), parametros, null);
            documentoDescarga = new DefaultStreamedContent(is, "application/pdf",
                    persona.getIdentificacion() + "_FormularioResultado" + ".pdf");

        } catch (Exception ex) {
            Logger.getLogger(RegistroPostulanteControlador.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                is.close();
            } catch (IOException ex) {
                Logger.getLogger(RegistroPostulanteControlador.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return documentoDescarga;
    }

    public void setDocumentoDescarga(StreamedContent documentoDescarga) {
        this.documentoDescarga = documentoDescarga;
    }

    public Map<String, String> getParametros() {
        return parametros;
    }

    public void setParametros(Map<String, String> parametros) {
        this.parametros = parametros;
    }

    public StreamedContent getDescargarPostulacion() {
        InputStream is = null;
        try {
            parametros.put("ID_POSTULANTE", String.valueOf(inscripcionSeleccionada.getId()));
            is = servicioReporte.getReporte(
                    "24", "/reports/habilitacion/Registro/RepPostulacionHabilitacion",
                    persona.getIdentificacion(), parametros, null);
            descargarPostulacion = new DefaultStreamedContent(is, "application/pdf",
                    persona.getIdentificacion() + "_FormularioPostulacion" + ".pdf");

        } catch (Exception ex) {
            Logger.getLogger(DatosPostulacionControlador.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                is.close();
            } catch (IOException ex) {
                Logger.getLogger(DatosPostulacionControlador.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return descargarPostulacion;
    }

    public Inscripcion getInscripcionSeleccionada() {
        return inscripcionSeleccionada;
    }

    public void setInscripcionSeleccionada(Inscripcion inscripcionSeleccionada) {
        this.inscripcionSeleccionada = inscripcionSeleccionada;
    }

    public void setDescargarPostulacion(StreamedContent descargarPostulacion) {
        this.descargarPostulacion = descargarPostulacion;
    }

    public List<DocumentoProcesoExamen> getListaDocumentoGenerales() {
        return listaDocumentoGenerales;
    }

    public void setListaDocumentoGenerales(List<DocumentoProcesoExamen> listaDocumentoGenerales) {
        this.listaDocumentoGenerales = listaDocumentoGenerales;
    }

    public DocumentoProcesoExamen getDocumentoSeleccionado() {
        return documentoSeleccionado;
    }

    public void setDocumentoSeleccionado(DocumentoProcesoExamen documentoSeleccionado) {
        this.documentoSeleccionado = documentoSeleccionado;
    }

    public StreamedContent getDocumentosGeneralesDescarga() throws FileNotFoundException {
        File file = new File(documentoSeleccionado.getPathDocumento());

        InputStream is = new FileInputStream(file);

        this.documentosGeneralesDescarga = new DefaultStreamedContent(is,
                "pdf", documentoSeleccionado.getNombreDocumento());

        return documentosGeneralesDescarga;
    }

    public void setDocumentosGeneralesDescarga(StreamedContent documentosGeneralesDescarga) {
        this.documentosGeneralesDescarga = documentosGeneralesDescarga;
    }
    
   
    public boolean isDocumentosProcesos() {
        return documentosProcesos;
    }

    public void setDocumentosProcesos(boolean documentosProcesos) {
        this.documentosProcesos = documentosProcesos;
    }

    public List<DocumentoProcesoExamen> getListaDocumentoProceso() {
        return listaDocumentoProceso;
    }

    public void setListaDocumentoProceso(List<DocumentoProcesoExamen> listaDocumentoProceso) {
        this.listaDocumentoProceso = listaDocumentoProceso;
    }

    public StreamedContent getDescargarHojaExamen() throws FileNotFoundException {
        try{
        File file = new File(inscripcionSeleccionada.getResultadoExamen().getPathResultadoExamen());

        InputStream is = new FileInputStream(file);

        this.descargarHojaExamen = new DefaultStreamedContent(is,
                "image/jpg", inscripcionSeleccionada.getId().toString() + "_" + persona.getIdentificacion());
        return descargarHojaExamen;
        }catch (Exception ex){
         Logger.getLogger(DatosPostulacionControlador.class.getName()).log(Level.SEVERE, null, ex);
            JsfUtil.msgError("No existe el fichero o el directorio");
        
        }
       return descargarHojaExamen;
    }

    public void setDescargarHojaExamen(StreamedContent descargarHojaExamen) {
        this.descargarHojaExamen = descargarHojaExamen;
    }

}
