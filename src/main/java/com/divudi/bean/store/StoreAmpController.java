/*
 * MSc(Biomedical Informatics) Project
 *
 * Development and Implementation of a Web-based Combined Data Repository of
 Genealogical, Clinical, Laboratory and Genetic Data
 * and
 * a Set of Related Tools
 */
package com.divudi.bean.store;

import com.divudi.bean.common.SessionController;
import com.divudi.bean.common.UtilityController;
import com.divudi.data.DepartmentType;
import com.divudi.ejb.BillNumberGenerator;
import com.divudi.entity.pharmacy.Amp;
import com.divudi.facade.AmpFacade;
import com.divudi.facade.util.JsfUtil;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, PGIM Trainee for MSc(Biomedical
 * Informatics)
 */
@Named
@SessionScoped
public class StoreAmpController implements Serializable {

    private static final long serialVersionUID = 1L;
    @Inject
    SessionController sessionController;
    @EJB
    private AmpFacade ejbFacade;
    private Amp current;
    private List<Amp> items = null;
    private List<Amp> itemsAll = null;
    List<Amp> itemsByCode = null;

    public List<Amp> getItemsByCode() {
        if (itemsByCode == null) {
            itemsByCode = getFacade().findBySQL("select a from Amp a where a.retired=false order by a.code");
        }
        return itemsByCode;
    }

    public void setItemsByCode(List<Amp> itemsByCode) {
        this.itemsByCode = itemsByCode;
    }

    @EJB
    BillNumberGenerator billNumberBean;

    public void prepareAdd() {
        current = null;
    }

    public void recreateModel() {
        items = null;
        current = null;
    }

    public void saveSelected() {

        if (getCurrent().getId() != null && getCurrent().getId() > 0) {
            getFacade().edit(current);
            UtilityController.addSuccessMessage("Updated Successfully.");
        } else {
            getCurrent().setCreatedAt(new Date());
            getCurrent().setCreater(getSessionController().getLoggedUser());
            getFacade().create(getCurrent());
            UtilityController.addSuccessMessage("Saved Successfully");
        }
        items = null;
        // getItems();
    }

    public AmpFacade getEjbFacade() {
        return ejbFacade;
    }

    public void setEjbFacade(AmpFacade ejbFacade) {
        this.ejbFacade = ejbFacade;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public StoreAmpController() {
    }

    public Amp getCurrent() {
        if (current == null) {
            current = new Amp();
            current.setDepartmentType(DepartmentType.Store);
//            current.setCode(billNumberBean.storeItemNumberGenerator());
        }
        return current;
    }

    public void setCurrent(Amp current) {
        this.current = current;

    }

    public void delete() {

        if (current != null) {
            current.setRetired(true);
            current.setRetiredAt(new Date());
            current.setRetirer(getSessionController().getLoggedUser());
            getFacade().edit(current);
            UtilityController.addSuccessMessage("Deleted Successfully");
        } else {
            UtilityController.addSuccessMessage("Nothing to Delete");
        }
        recreateModel();
        getItems();
        current = null;
        getCurrent();
    }

    private AmpFacade getFacade() {
        return ejbFacade;
    }
    private List<Amp> filteredItems;

    public List<Amp> getItems() {
        if (items == null) {
            Map m = new HashMap();
            m.put("dt", DepartmentType.Store);
            String sql = "Select a from Item a where a.retired=false and a.departmentType=:dt order by a.name";
            items = getFacade().findBySQL(sql, m);
        }
        return items;
    }

    public void createStoreItemsWithRetierd() {
        items = null;
        getItems();
        itemsAll.addAll(items);
        Map m = new HashMap();
        m.put("dt", DepartmentType.Store);
        String sql = "Select a from Item a where a.retired=true and a.departmentType=:dt order by a.name";
        itemsAll.addAll(getFacade().findBySQL(sql, m));
    }

    public List<Amp> getFilteredItems() {
        return filteredItems;
    }

    public void setFilteredItems(List<Amp> filteredItems) {
        this.filteredItems = filteredItems;
    }

    public List<Amp> getItemsAll() {
        if (itemsAll == null) {
            itemsAll = new ArrayList<>();
        }
        return itemsAll;
    }

    public void setItemsAll(List<Amp> itemsAll) {
        this.itemsAll = itemsAll;
    }

    public void listnerCategorySelect() {
        // //// System.out.println("getCurrent().getCategory().getCode() = " + getCurrent().getCategory().getCode());
        if (getCurrent().getCategory().getCode() == null || getCurrent().getCategory().getCode().equals("")) {
            JsfUtil.addErrorMessage("Please Select Category Code");
            getCurrent().setCode("");
            return;
        }

        Map m = new HashMap();
        String sql = "select c from Amp c "
                + " where c.retired=false"
                + " and c.category=:cat "
                + " and c.departmentType=:dep "
                + " order by c.code desc";

        m.put("dep", DepartmentType.Store);
        m.put("cat", getCurrent().getCategory());

        Amp amp = getFacade().findFirstBySQL(sql, m);


        DecimalFormat df = new DecimalFormat("0000");
        if (amp != null && !amp.getCode().equals("")) {
            // //// System.out.println("amp.getCode() = " + amp.getCode());

            String s = amp.getCode().substring(getCurrent().getCategory().getCode().length());
            // //// System.out.println("s = " + s);

            int i = Integer.valueOf(s);
            // //// System.out.println("i = " + i);
            i++;
            if (getCurrent().getId() != null) {
                Amp selectedAmp = getFacade().find(getCurrent().getId());
                if (!getCurrent().getCategory().equals(selectedAmp.getCategory())) {
                    getCurrent().setCode(getCurrent().getCategory().getCode() + df.format(i));
                } else {
                    getCurrent().setCode(selectedAmp.getCode());
                }
            } else {
                getCurrent().setCode(getCurrent().getCategory().getCode() + df.format(i));
            }
        } else {
            getCurrent().setCode(getCurrent().getCategory().getCode() + df.format(1));
        }

    }

    /**
     *
     */
    @FacesConverter("stoAmpCon")
    public static class AmpControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            StoreAmpController controller = (StoreAmpController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "storeAmpController");
            return controller.getEjbFacade().find(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            key = Long.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Amp) {
                Amp o = (Amp) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type "
                        + object.getClass().getName() + "; expected type: " + StoreAmpController.class.getName());
            }
        }
    }
}
