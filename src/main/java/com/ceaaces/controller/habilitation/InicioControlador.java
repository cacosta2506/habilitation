/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.gob.ceaaces.habilitacion.controlador;

import ec.gob.ceaaces.habilitacion.modelo.Inscripcion;
import ec.gob.ceaaces.habilitacion.modelo.ProcesoExamen;
import ec.gob.ceaaces.habilitacion.modelo.persona.Persona;
import ec.gob.ceaaces.habilitacion.proxy.ServicioHabilitacion;
import ec.gob.ceaaces.habilitacion.servicio.exception.ServicioHabilitacionException;
import ec.gob.ceaaces.habilitacion.servicio.wrapper.Resultado;
import ec.gob.ceaaces.habilitacion.util.JsfUtil;
import ec.gob.ceaaces.habilitacion.util.Util;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.apache.commons.lang.BooleanUtils;

/**
 *
 * @author CAM
 */
@ManagedBean
@RequestScoped
public class InicioControlador implements Serializable {

    private static final long serialVersionUID = 3454104667169651774L;
    private static final Logger LOG = Logger.getLogger(InicioControlador.class.getName());

    // CAM: Variables de control de acceso
    private boolean login = false;
    private boolean crearRegistro = false;
    private boolean registroPostulante = false;
    private boolean datosPersonales = false;
    private boolean inscripcionPostulante = false;
    private boolean datosInscripcion = false;
    private boolean ingresarPostulacion = false;
    private List<Inscripcion> listaInscripciones;
    private String cedula;
    private boolean nada = true;
    private boolean mostrarTokenPanel = false;
    private String codigoToken;
    private int valor = 2;
    private Resultado resultadoEnvioEmail;
    private Persona persona;
    private List<ProcesoExamen> listaProcesoExamen;
    private String tipo_identificacion;
    @Inject
    private ServicioHabilitacion servicioHabilitacion;

    public InicioControlador() {
        LOG.info("Se inicia controlador");
        persona = new Persona();
        listaProcesoExamen = new ArrayList<>();
        listaInscripciones = new ArrayList<>();
        resultadoEnvioEmail = new Resultado();

    }

    @PostConstruct
    private void cargarDatosIniciales() {
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ConstantesSesion.CREAR_REGISTRO, crearRegistro);
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ConstantesSesion.DATOS_PERSONALES, datosPersonales);
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ConstantesSesion.REGISTRO_POSTULANTE, registroPostulante);
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ConstantesSesion.INSCRIPCION_POSTULANTE, inscripcionPostulante);
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ConstantesSesion.DATOS_INSCRIPCION, datosInscripcion);
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ConstantesSesion.LOGIN, login);
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ConstantesSesion.RESULTADO_ENVIO_EMAIL, resultadoEnvioEmail);

    }

    /**
     *
     * @return
     */
    public String devolverFormulario() {
        listaProcesoExamen = servicioHabilitacion.obtenerProcesosExamenPorFase(1L);
        if (BooleanUtils.isFalse(listaProcesoExamen.isEmpty())) {
            if (valor == 2) {
                if (Util.validarCedula(cedula)) {
                    try {
                        tipo_identificacion = "CEDULA";
                        persona = servicioHabilitacion.obtenerPersonaPorIdentificacion(tipo_identificacion, cedula);
                        if (null != persona.getId()) {

                            switch (persona.getEstadoPersona().name()) {
                                case "REGISTRADO":
                                    try {
                                        listaInscripciones = servicioHabilitacion.obtenerInscripcionesActivas(persona.getIdentificacion());
                                        if (BooleanUtils.isTrue(listaInscripciones.isEmpty())) {
                                            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ConstantesSesion.INGRESAR_POSTULACION, false);
                                        } else {
                                            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ConstantesSesion.INGRESAR_POSTULACION, true);
                                        }
                                        resultadoEnvioEmail = servicioHabilitacion.enviarEmailConToken(persona.getId());
                                        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ConstantesSesion.RESULTADO_ENVIO_EMAIL, resultadoEnvioEmail);
                                        if (resultadoEnvioEmail.getExitoMail()) {
                                            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ConstantesSesion.PERSONA, persona);
                                            //CAM:  Variable de control de acceso a la pagina siguiente si se mantiene en false no puedes ingresar.
                                            login = true;
                                            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ConstantesSesion.LOGIN, login);
                                            return "login?faces-redirect=true";
                                        }

                                        //return "registroPostulante?faces-redirect=true";
                                    } catch (Exception ex) {
                                        mostrarTokenPanel = false;
                                        JsfUtil.msgError(ex.getMessage());
                                        Logger.getLogger(InicioControlador.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                    break;
                                case "REPORTADO":
                                    FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ConstantesSesion.PERSONA, persona);
                                    //CAM:  Variable de control de acceso a la pagina siguiente si se mantiene en false no puedes ingresar.
                                    crearRegistro = true;
                                    FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ConstantesSesion.CREAR_REGISTRO, crearRegistro);
                                    return "crearRegistro?faces-redirect=true";
                            }

                        } else {
                            persona.setDocumentoIdentificacion(tipo_identificacion);
                            persona.setIdentificacion(cedula);
                            persona.setOrigen("SERVICIO");
                            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ConstantesSesion.PERSONA, persona);
                            //CAM:  Variable de control de acceso a la pagina siguiente si se mantiene en false no puedes ingresar.
                            crearRegistro = true;
                            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ConstantesSesion.CREAR_REGISTRO, crearRegistro);
                            return "crearRegistro?faces-redirect=true";
                        }
                    } catch (ServicioHabilitacionException ex) {
                        Logger.getLogger(InicioControlador.class.getName()).log(Level.SEVERE, null, ex);
                        JsfUtil.msgError(ex.getMessage());
                    }

                } else if (!Util.validarCedula(cedula)) {
                    JsfUtil.msgError("Cédula Incorrecta");
                }
            } else if (valor == 1) {
                try {
                    tipo_identificacion = "PASAPORTE";
                    persona = servicioHabilitacion.obtenerPersonaPorIdentificacion(tipo_identificacion, cedula);
                    if (null != persona.getId()) {

                        switch (persona.getEstadoPersona().name()) {
                            case "REGISTRADO":
                                try {
                                    listaInscripciones = servicioHabilitacion.obtenerInscripcionesActivas(persona.getIdentificacion());
                                    if (BooleanUtils.isTrue(listaInscripciones.isEmpty())) {
                                        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ConstantesSesion.INGRESAR_POSTULACION, false);
                                    } else {
                                        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ConstantesSesion.INGRESAR_POSTULACION, true);
                                    }
                                    resultadoEnvioEmail = servicioHabilitacion.enviarEmailConToken(persona.getId());
                                    FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ConstantesSesion.RESULTADO_ENVIO_EMAIL, resultadoEnvioEmail);
                                    if (resultadoEnvioEmail.getExitoMail()) {
                                        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ConstantesSesion.PERSONA, persona);
                                        //CAM:  Variable de control de acceso a la pagina siguiente si se mantiene en false no puedes ingresar.
                                        login = true;
                                        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ConstantesSesion.LOGIN, login);
                                        return "login?faces-redirect=true";
                                    }

                                    //return "registroPostulante?faces-redirect=true";
                                } catch (Exception ex) {
                                    mostrarTokenPanel = false;
                                    JsfUtil.msgError(ex.getMessage());
                                    Logger.getLogger(InicioControlador.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                break;
                            case "REPORTADO":
                                FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ConstantesSesion.PERSONA, persona);
                                //TODO CAM:  Variable de control de acceso a la pagina siguiente si se mantiene en false no puedes ingresar.
                                crearRegistro = true;
                                FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ConstantesSesion.CREAR_REGISTRO, crearRegistro);
                                return "crearRegistro?faces-redirect=true";
                        }

                    } else {
                        persona.setDocumentoIdentificacion(tipo_identificacion);
                        persona.setIdentificacion(cedula);
                        persona.setOrigen("SERVICIO");
                        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ConstantesSesion.PERSONA, persona);
                        //TODO CAM:  Variable de control de acceso a la pagina siguiente si se mantiene en false no puedes ingresar.
                        crearRegistro = true;
                        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ConstantesSesion.CREAR_REGISTRO, crearRegistro);
                        return "crearRegistro?faces-redirect=true";
                    }
                } catch (ServicioHabilitacionException ex) {
                    Logger.getLogger(InicioControlador.class.getName()).log(Level.SEVERE, null, ex);
                    JsfUtil.msgError(ex.getMessage());
                }
            }
        } else {
            if (valor == 2) {
                if (Util.validarCedula(cedula)) {
                    try {
                        tipo_identificacion = "CEDULA";
                        persona = servicioHabilitacion.obtenerPersonaPorIdentificacion(tipo_identificacion, cedula);
                        if (null != persona.getId()) {

                            switch (persona.getEstadoPersona().name()) {
                                case "REGISTRADO":
                                    try {

                                        resultadoEnvioEmail = servicioHabilitacion.enviarEmailConToken(persona.getId());
                                        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ConstantesSesion.RESULTADO_ENVIO_EMAIL, resultadoEnvioEmail);
                                        if (resultadoEnvioEmail.getExitoMail()) {
                                            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ConstantesSesion.PERSONA, persona);
                                            //CAM:  Variable de control de acceso a la pagina siguiente si se mantiene en false no puedes ingresar.
                                            login = true;
                                            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ConstantesSesion.LOGIN, login);
                                            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ConstantesSesion.INGRESAR_POSTULACION, true);
                                            return "login?faces-redirect=true";
                                        }

                                        //return "registroPostulante?faces-redirect=true";
                                    } catch (Exception ex) {
                                        mostrarTokenPanel = false;
                                        JsfUtil.msgError(ex.getMessage());
                                        Logger.getLogger(InicioControlador.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                    break;
                                case "REPORTADO":
                                    JsfUtil.msgError("Usted no realizó el proceso de habilitación");

                                    return "";
                            }

                        }
                    } catch (ServicioHabilitacionException ex) {
                        Logger.getLogger(InicioControlador.class.getName()).log(Level.SEVERE, null, ex);
                        JsfUtil.msgError(ex.getMessage());
                    }

                } else if (!Util.validarCedula(cedula)) {
                    JsfUtil.msgError("Cédula Incorrecta");
                }
            } else if (valor == 1) {
                try {
                    tipo_identificacion = "PASAPORTE";
                    persona = servicioHabilitacion.obtenerPersonaPorIdentificacion(tipo_identificacion, cedula);
                    if (null != persona.getId()) {

                        listaProcesoExamen = servicioHabilitacion.obtenerProcesosExamenPorFase(1L);
                        if (BooleanUtils.isFalse(listaProcesoExamen.isEmpty())) {

                            switch (persona.getEstadoPersona().name()) {
                                case "REGISTRADO":
                                    try {

                                        resultadoEnvioEmail = servicioHabilitacion.enviarEmailConToken(persona.getId());
                                        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ConstantesSesion.RESULTADO_ENVIO_EMAIL, resultadoEnvioEmail);
                                        if (resultadoEnvioEmail.getExitoMail()) {
                                            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ConstantesSesion.PERSONA, persona);
                                            //CAM:  Variable de control de acceso a la pagina siguiente si se mantiene en false no puedes ingresar.
                                            login = true;
                                            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ConstantesSesion.LOGIN, login);
                                            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ConstantesSesion.INGRESAR_POSTULACION, true);
                                            return "login?faces-redirect=true";
                                        }

                                        //return "registroPostulante?faces-redirect=true";
                                    } catch (Exception ex) {
                                        mostrarTokenPanel = false;
                                        JsfUtil.msgError(ex.getMessage());
                                        Logger.getLogger(InicioControlador.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                    break;
                                case "REPORTADO":
                                    JsfUtil.msgError("Usted no realizó el proceso de habilitación");
                                    return "";
                            }
                        }

                    }
                } catch (ServicioHabilitacionException ex) {
                    Logger.getLogger(InicioControlador.class.getName()).log(Level.SEVERE, null, ex);
                    JsfUtil.msgError(ex.getMessage());
                }
            }

        }
        return "";

    }

    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }

    public boolean isNada() {
        return nada;
    }

    public void setNada(boolean nada) {
        this.nada = nada;
    }

    public int getValor() {
        return valor;
    }

    public void setValor(int valor) {
        this.valor = valor;
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

    public Resultado getResultadoEnvioEmail() {
        return resultadoEnvioEmail;
    }

    public void setResultadoEnvioEmail(Resultado resultadoEnvioEmail) {
        this.resultadoEnvioEmail = resultadoEnvioEmail;
    }

    public boolean isMostrarTokenPanel() {
        return mostrarTokenPanel;
    }

    public void setMostrarTokenPanel(boolean mostrarTokenPanel) {
        this.mostrarTokenPanel = mostrarTokenPanel;
    }

    public String getCodigoToken() {
        return codigoToken;
    }

    public void setCodigoToken(String codigoToken) {
        this.codigoToken = codigoToken;
    }

    public boolean isRegistroPostulante() {
        return registroPostulante;
    }

    public void setRegistroPostulante(boolean registroPostulante) {
        this.registroPostulante = registroPostulante;
    }

    public boolean isDatosPersonales() {
        return datosPersonales;
    }

    public void setDatosPersonales(boolean datosPersonales) {
        this.datosPersonales = datosPersonales;
    }

    public boolean isInscripcionPostulante() {
        return inscripcionPostulante;
    }

    public void setInscripcionPostulante(boolean inscripcionPostulante) {
        this.inscripcionPostulante = inscripcionPostulante;
    }

    public boolean isDatosInscripcion() {
        return datosInscripcion;
    }

    public void setDatosInscripcion(boolean datosInscripcion) {
        this.datosInscripcion = datosInscripcion;
    }

    public boolean isCrearRegistro() {
        return crearRegistro;
    }

    public void setCrearRegistro(boolean crearRegistro) {
        this.crearRegistro = crearRegistro;
    }

    public boolean isLogin() {
        return login;
    }

    public void setLogin(boolean login) {
        this.login = login;
    }

    public List<Inscripcion> getListaInscripciones() {
        return listaInscripciones;
    }

    public void setListaInscripciones(List<Inscripcion> listaInscripciones) {
        this.listaInscripciones = listaInscripciones;
    }

    public boolean isIngresarPostulacion() {
        return ingresarPostulacion;
    }

    public void setIngresarPostulacion(boolean ingresarPostulacion) {
        this.ingresarPostulacion = ingresarPostulacion;
    }

}
