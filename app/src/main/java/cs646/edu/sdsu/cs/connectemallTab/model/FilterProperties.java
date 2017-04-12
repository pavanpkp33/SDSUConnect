package cs646.edu.sdsu.cs.connectemallTab.model;

import java.util.ArrayList;

/**
 * Created by Pkp on 04/07/17.
 */

public class FilterProperties {

    private int selectedState;
    private int selectedCountry;
    private String year;
    private ArrayList<String> countryList;
    private ArrayList<String> stateList;

    public FilterProperties(){

        this.selectedState = 0;
        this.selectedCountry = 0;
        this.year = "";
    }

    public int getSelectedState() {
        return selectedState;
    }

    public void setSelectedState(int selectedState) {
        this.selectedState = selectedState;
    }

    public int getSelectedCountry() {
        return selectedCountry;
    }

    public void setSelectedCountry(int selectedCountry) {
        this.selectedCountry = selectedCountry;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public ArrayList<String> getCountryList() {
        return countryList;
    }

    public void setCountryList(ArrayList<String> countryList) {
        this.countryList = countryList;
    }

    public ArrayList<String> getStateList() {
        return stateList;
    }

    public void setStateList(ArrayList<String> stateList) {
        this.stateList = stateList;
    }
}
