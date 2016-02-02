/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.gob.ceaaces.habilitacion.controlador;

import ec.gob.ceaaces.catalogo.modelo.Proceso;
import ec.gob.ceaaces.habilitacion.controlador.auth.LogoutControlador;
import ec.gob.ceaaces.habilitacion.enumeracion.EnumFormaExamen;
import ec.gob.ceaaces.habilitacion.enumeracion.EnumMotivoCambioSede;
import ec.gob.ceaaces.habilitacion.enumeracion.EnumOrigenInscripcion;
import ec.gob.ceaaces.habilitacion.modelo.Inscripcion;
import ec.gob.ceaaces.habilitacion.modelo.ProcesoExamen;
import ec.gob.ceaaces.habilitacion.modelo.admin.SedeProceso;
import ec.gob.ceaaces.habilitacion.modelo.persona.Persona;
import ec.gob.ceaaces.habilitacion.modelo.persona.PersonaCarreraIes;
import ec.gob.ceaaces.habilitacion.modelo.persona.PersonaTituloProfesional;
import ec.gob.ceaaces.habilitacion.proxy.ServicioHabilitacion;
import ec.gob.ceaaces.habilitacion.servicio.exception.ServicioHabilitacionException;
import ec.gob.ceaaces.habilitacion.servicio.impl.ServicioInscripcion;
import ec.gob.ceaaces.habilitacion.util.JsfUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

/**
 *
 * @author CAM
 */
@ManagedBean
@ViewScoped
public class InscripcionPostulanteControlador implements Serializable {

    private static final long serialVersionUID = 3922545765055705859L;
    private static final Logger LOG = Logger.getLogger(InscripcionPostulanteControlador.class.getName());

    private Persona persona;
    private Long idProceso;
    private Long idSede;
    private Proceso proceso;
    private boolean mostrarSedes = false;
    private boolean cambioSede = false;
    private boolean inscripcionPostulante;
    private List<SedeProceso> listaSedeProceso;
    private SedeProceso sedeProceso;
    private SedeProceso sedeProcesoTemp;
    private Inscripcion inscripcion;
    private List<ProcesoExamen> listaProcesoExamen;
    private List<PersonaCarreraIes> listaPersonaCarrera;
    private List<PersonaTituloProfesional> listaTitulos;
    private PersonaCarreraIes carreraSeleccionada;
    private PersonaTituloProfesional tituloSeleccionado;
    private ProcesoExamen procesoExamenSeleccionado;
    private List<Inscripcion> listaInscripciones;
    private List<EnumMotivoCambioSede> listaCambioSede;
    private String tipo_identificacion;
    private String tipoExamen;

    @Inject
    private ServicioHabilitacion servicioHabilitacion;

    @EJB(lookup = "java:global/habilitacionCeaaces-servicio/ServicioInscripcion!ec.gob.ceaaces.habilitacion.servicio.impl.ServicioInscripcion")
    private ServicioInscripcion servicioInscripcion;

    @Inject
    private LogoutControlador logoutControlador;

    public InscripcionPostulanteControlador() {

        persona = new Persona();
        proceso = new Proceso();
        inscripcion = new Inscripcion();
        listaProcesoExamen = new ArrayList<>();
        listaPersonaCarrera = new ArrayList<>();
        listaTitulos = new ArrayList<>();
        listaInscripciones = new ArrayList<>();
        procesoExamenSeleccionado = new ProcesoExamen();
        listaSedeProceso = new ArrayList<>();
        listaCambioSede = new ArrayList<>();
        sedeProceso = new SedeProceso();
        sedeProcesoTemp = new SedeProceso();
        carreraSeleccionada = new PersonaCarreraIes();
        tituloSeleccionado = new PersonaTituloProfesional();
    }

    @PostConstruct
    private void cargarDatosIniciales() {

        persona = (Persona) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(ConstantesSesion.PERSONA);
        proceso = (Proceso) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(ConstantesSesion.PROCESO);
        tipoExamen = (String) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(ConstantesSesion.TIPO_EXAMEN);
        inscripcionPostulante = (boolean) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(ConstantesSesion.INSCRIPCION_POSTULANTE);
        if (persona != null && proceso != null) {
//        listaProcesoExamen = servicioHabilitacion.obtenerProcesosExamenPorFase(1L);
//        listaInscripciones = servicioHabilitacion.obtenerInscripcionesPorPersona(persona.getIdentificacion());
            if (persona.getOrigen().equals("CARGA_MASIVA")) {
                listaPersonaCarrera = servicioHabilitacion.obtenerPersonaCarreraIes(persona.getIdentificacion(), proceso.getIdGrupoCarrera());
            } else if (persona.getOrigen().equals("SERVICIO")) {
                listaTitulos = servicioHabilitacion.obtenerPersonaFormacionProfesional(persona.getIdentificacion());
            }
        } else {
            LOG.info("Proceso y persona es nulo");
        }
    }

    public String cancelar() {
        logoutControlador.logout();
        return "inicio?faces-redirect=true";
    }

    public String redirect() {

        try {
            inscripcion.setPersona(persona);
            switch (persona.getOrigen()) {
                case "CARGA_MASIVA":
                    inscripcion.setOrigenInscripcion(EnumOrigenInscripcion.PERSONA_CARRERA_IES);
                    inscripcion.setPersonaTituloProfesional(null);
                    inscripcion.setPersonaCarreraIes(carreraSeleccionada);
                    if (!idSede.equals(sedeProceso.getId())) {
                        for (SedeProceso sede : listaSedeProceso) {
                            if (idSede.equals(sede.getId())) {
                                inscripcion.setSedeProceso(sede);
                                break;
                            }
                        }
                    } else {
                        inscripcion.setSedeProceso(sedeProceso);
                    }
                    break;
                case "SERVICIO":
                    inscripcion.setOrigenInscripcion(EnumOrigenInscripcion.PERSONA_TITULO_PROFESIONAL);
                    inscripcion.setPersonaCarreraIes(null);
                    tituloSeleccionado.setPersona(persona);
                    inscripcion.setPersonaTituloProfesional(tituloSeleccionado);

                    for (SedeProceso sede : listaSedeProceso) {
                        if (idSede.equals(sede.getId())) {
                            inscripcion.setSedeProceso(sede);
                            break;
                        }
                    }

                    break;
            }

            inscripcion.setFormaExamen(EnumFormaExamen.FORMA1);
            servicioInscripcion.registrarInscripcion(inscripcion);
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ConstantesSesion.INSCRIPCION, inscripcion);
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ConstantesSesion.INSCRIPCION_POSTULANTE, false);
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ConstantesSesion.DATOS_PERSONALES, false);
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ConstantesSesion.REGISTRO_POSTULANTE, false);
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ConstantesSesion.LOGIN, false);
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ConstantesSesion.DATOS_INSCRIPCION, true);
            return "datosInscripcion?faces-redirect=true";
        } catch (ServicioHabilitacionException ex) {
            Logger.getLogger(InscripcionPostulanteControlador.class.getName()).log(Level.SEVERE, null, ex);
            JsfUtil.msgError(ex.getMessage());
        }
        return "";

    }

    public void controlNavegacion() {
        if (!inscripcionPostulante) {
            FacesContext context1 = FacesContext.getCurrentInstance();
            ConfigurableNavigationHandler handler = (ConfigurableNavigationHandler) context1
                    .getApplication().getNavigationHandler();
            handler.performNavigation("inicio?faces-redirect=true");
        }
    }

    public void cargarSedes() {
        try {
            listaSedeProceso = servicioHabilitacion.obtenerSedesProcesoPorProceso(proceso.getId());
            if (persona.getOrigen().equals("CARGA_MASIVA")) {
                sedeProceso = servicioHabilitacion.obtenerSedeProcesoPorIes(carreraSeleccionada.getIes().getId(), proceso.getId());
                idSede = sedeProceso.getId();
                sedeProcesoTemp = sedeProceso;
            } else if (persona.getOrigen().equals("SERVICIO")) {
                idSede = listaSedeProceso.get(0).getSede().getId();
                sedeProcesoTemp = listaSedeProceso.get(0);
            }
            mostrarSedes = true;

        } catch (ServicioHabilitacionException ex) {
            Logger.getLogger(InscripcionPostulanteControlador.class.getName()).log(Level.SEVERE, null, ex);
            JsfUtil.msgError(ex.getMessage());
        }
    }

    public void cargarMotivos() {
        listaCambioSede.clear();
        listaCambioSede.addAll(Arrays.asList(EnumMotivoCambioSede.values()));
    }

    public void cargarMotivoCambio() {
        switch (persona.getOrigen()) {
            case "CARGA_MASIVA":
                if (!idSede.equals(sedeProceso.getId())) {
                    cambioSede = true;
                    cargarMotivos();
                    for (SedeProceso sede : listaSedeProceso) {
                        if (idSede.equals(sede.getId())) {
                            sedeProcesoTemp = sede;
                            break;
                        }
                    }
                } else {
                    cambioSede = false;
                    inscripcion.setMotivoCambioSede(null);
                    sedeProcesoTemp = sedeProceso;
                }
                break;
            case "SERVICIO":

                for (SedeProceso sede : listaSedeProceso) {
                    if (idSede.equals(sede.getId())) {
                        sedeProcesoTemp = sede;
                        break;
                    }
                    cambioSede = false;
                }
                break;
        }

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

    public Long getIdProceso() {
        return idProceso;
    }

    public void setIdProceso(Long idProceso) {
        this.idProceso = idProceso;
    }

    public Long getIdSede() {
        return idSede;
    }

    public void setIdSede(Long idSede) {
        this.idSede = idSede;
    }

    public List<SedeProceso> getListaSedeProceso() {
        return listaSedeProceso;
    }

    public void setListaSedeProceso(List<SedeProceso> listaSedeProceso) {
        this.listaSedeProceso = listaSedeProceso;
    }

    public List<PersonaCarreraIes> getListaPersonaCarrera() {
        return listaPersonaCarrera;
    }

    public void setListaPersonaCarrera(List<PersonaCarreraIes> listaPersonaCarrera) {
        this.listaPersonaCarrera = listaPersonaCarrera;
    }

    public PersonaCarreraIes getCarreraSeleccionada() {
        return carreraSeleccionada;
    }

    public void setCarreraSeleccionada(PersonaCarreraIes carreraSeleccionada) {
        this.carreraSeleccionada = carreraSeleccionada;
    }

    public SedeProceso getSedeProceso() {
        return sedeProceso;
    }

    public void setSedeProceso(SedeProceso sedeProceso) {
        this.sedeProceso = sedeProceso;
    }

    public boolean isMostrarSedes() {
        return mostrarSedes;
    }

    public void setMostrarSedes(boolean mostrarSedes) {
        this.mostrarSedes = mostrarSedes;
    }

    public boolean isCambioSede() {
        return cambioSede;
    }

    public void setCambioSede(boolean cambioSede) {
        this.cambioSede = cambioSede;
    }

    public Inscripcion getInscripcion() {
        return inscripcion;
    }

    public void setInscripcion(Inscripcion inscripcion) {
        this.inscripcion = inscripcion;
    }

    public SedeProceso getSedeProcesoTemp() {
        return sedeProcesoTemp;
    }

    public void setSedeProcesoTemp(SedeProceso sedeProcesoTemp) {
        this.sedeProcesoTemp = sedeProcesoTemp;
    }

    public List<PersonaTituloProfesional> getListaTitulos() {
        return listaTitulos;
    }

    public void setListaTitulos(List<PersonaTituloProfesional> listaTitulos) {
        this.listaTitulos = listaTitulos;
    }

    public PersonaTituloProfesional getTituloSeleccionado() {
        return tituloSeleccionado;
    }

    public void setTituloSeleccionado(PersonaTituloProfesional tituloSeleccionado) {
        this.tituloSeleccionado = tituloSeleccionado;
    }

    public List<EnumMotivoCambioSede> getListaCambioSede() {
        return listaCambioSede;
    }

    public void setListaCambioSede(List<EnumMotivoCambioSede> listaCambioSede) {
        this.listaCambioSede = listaCambioSede;
    }

    public boolean isInscripcionPostulante() {
        return inscripcionPostulante;
    }

    public void setInscripcionPostulante(boolean inscripcionPostulante) {
        this.inscripcionPostulante = inscripcionPostulante;
    }

}
