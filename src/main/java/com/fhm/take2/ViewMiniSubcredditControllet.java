package com.fhm.take2;

import com.crdt.Subcreddit;
import com.crdt.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;

public class ViewMiniSubcredditControllet {

    @FXML private Label subDescription;
    @FXML private Label subName;
    @FXML private ImageView subPFP;
    private Subcreddit subcreddit;
    User currentUser;

    public void initData(Subcreddit subcreddit, User currentUser) {
        this.subcreddit = subcreddit;
        this.currentUser = currentUser;
        if (subcreddit == null)
            return;
        if(subcreddit.GetLogo() != null)
            subPFP.setImage(new Image(subcreddit.GetLogo().GetURL()));
        subName.setText(subcreddit.GetSubName());
        subDescription.setText(subcreddit.GetDescription());
    }

    public int getSubID() {
        return subcreddit.GetSubId();
    }

    @FXML
    public void onSubcredditClick() {
       try{
           FXMLLoader loader = new FXMLLoader(getClass().getResource("subcreddit.fxml"));
           Parent root = loader.load();

           SubcredditController subcredditController = loader.getController();
           subcredditController.InitData(subcreddit.GetSubId(), currentUser);

           Stage stage = (Stage) subName.getScene().getWindow();
           stage.setScene(new Scene(root));
       } catch (Exception e){
           e.printStackTrace();
       }

    }

}
