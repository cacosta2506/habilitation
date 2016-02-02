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
import ec.gob.ceaaces.habilitacion.modelo.Inscripcion;
import ec.gob.ceaaces.habilitacion.modelo.ProcesoExamen;
import ec.gob.ceaaces.habilitacion.modelo.persona.Persona;
import ec.gob.ceaaces.habilitacion.proxy.ServicioCatalogoHabilitacion;
import ec.gob.ceaaces.habilitacion.proxy.ServicioHabilitacion;
import ec.gob.ceaaces.habilitacion.util.JsfUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;

/**
 *
 * @author CAM
 */
@ManagedBean
@ViewScoped
public class DatosPersonalesControlador implements Serializable {

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
    private List<CantonIes> listaCantones;
    private List<ParroquiaIes> listaParroquias;
    private List<Pais> listaPaises;
    private boolean discapacidad = false;
    private Date fechaMax;
    private boolean porcentajeDiscapacidad = false;
    private boolean datosPersonales;
    private Long idRegion;
    private Long idPueblo;
    private Long idProceso;
    private boolean indigena = false;
    private String tipo_identificacion;

    @Inject
    private ServicioCatalogoHabilitacion servicioCatalogoHabilitacion;

    @Inject
    private LogoutControlador logoutControlador;

    public DatosPersonalesControlador() {

        persona = new Persona();
        listaProcesoExamen = new ArrayList<>();
        listaInscripciones = new ArrayList<>();
        listaEtnico = new ArrayList<>();
        listaDiscapacidad = new ArrayList<>();
        listaRegiones = new ArrayList<>();
        listaPueblo = new ArrayList<>();
        listaPaises = new ArrayList<>();
        listaProvincia = new ArrayList<>();
        listaDiscapacidad = new ArrayList<>();
        listaNivelDiscapacidad = new ArrayList<>();
        listaParroquias = new ArrayList<>();
        listaCantones = new ArrayList<>();
        procesoExamenSeleccionado = new ProcesoExamen();

    }

    @PostConstruct
    private void cargarDatosIniciales() {

        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(true);
        persona = (Persona) session.getAttribute(ConstantesSesion.PERSONA);
        datosPersonales = (boolean) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(ConstantesSesion.DATOS_PERSONALES);

        if (persona == null) {
            cancelar();
        } else {
            listaEtnico.clear();
            listaDiscapacidad = servicioCatalogoHabilitacion.obtenerDiscapacidades();
            listaEtnico.addAll(Arrays.asList(EtniaEnum.values()));
            cargarProvincia();
            cargarCanton();
            cargarParroquias();
            cargarPaises();
        }
    }

    public void controlNavegacion() {
        if (!datosPersonales) {
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

    public boolean isDatosPersonales() {
        return datosPersonales;
    }

    public void setDatosPersonales(boolean datosPersonales) {
        this.datosPersonales = datosPersonales;
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

}
