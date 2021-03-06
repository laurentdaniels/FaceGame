package facegame.userinterface;

import java.util.ArrayList;
import java.util.Collections;

import General.NPC;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.utils.Array;

import facegame.quests.QuestManager;
import facegame.quests.QuestProgress;
import facegame.quests.RewardManager;
import facegame.utils.NewAssetManager;

public class DialogStage extends Stage{
	
	private QuestManager questManager;
	private QuestProgress progressHUD;
	private Label rewardLabel;
	private Label questNameLabel;
	
	private int scrnWidth, scrnHeight;
	private boolean inDialog, interactionAvailable;
	
	public Label dialogBoxLabel, dialogNextLabel, interactLabel;
	private SpriteBatch batch;

	private Stage imageStage;
	private Image arrow;
	private Label targetDistance;
	private Image npcPortrait;	
	
	private int currentQuestIndex = 0;
	
	private NewAssetManager assetManager;
	
	public DialogStage(QuestManager questManager){
		super();
		this.questManager = questManager;
		
		scrnWidth = Gdx.graphics.getWidth(); 
		scrnHeight = Gdx.graphics.getHeight();
		
		batch = new SpriteBatch();
		
		initialize();
		
	}
	
	public void draw(float arrowRotation, float distance) {
		float delta = Gdx.graphics.getDeltaTime();
		
		batch.begin();
		if(inDialog){
			if(npcPortrait != null){
				npcPortrait.act(delta);
				npcPortrait.draw(batch, 1);
			}
			
			dialogBoxLabel.act(delta);
			dialogNextLabel.act(delta);
			dialogBoxLabel.draw(batch, 1);
			dialogNextLabel.draw(batch, 1);
			imageStage.act(delta);
			imageStage.draw();
			

		}
		else{
			imageStage.clear();
		}
		
		if(interactionAvailable && !inDialog){
			interactLabel.act(delta);
			interactLabel.draw(batch, 1);
		}
		
		if(!questManager.questsComplete()){
			arrow.setRotation(arrowRotation);
			arrow.act(delta);
			arrow.draw(batch, 1);

			questNameLabel.setText("Current Quest: "+questManager.getQuest().getName()+"\nReward Offered: "
					+questManager.getQuest().getReward()+"\nPenalty for Failure: "+questManager.getQuest().getPenalty());
			questNameLabel.draw(batch, 1);
			
			targetDistance.setText("Distance: " + (int)(distance/10));
			targetDistance.act(delta);
			targetDistance.draw(batch, 1);
		}
		else{
			if(!inDialog)
				((Game) Gdx.app.getApplicationListener()).setScreen(new EndGame(questManager));
		}

		rewardLabel.draw(batch, 1);
		progressHUD.draw(batch);
		batch.end();
	};
	
	public void initialize(){
		assetManager = NewAssetManager.getInstance();
		
		TextureAtlas textureAtlas = new TextureAtlas("dialog/dialog.pack");
		Skin skin = new Skin(Gdx.files.internal("dialog/dialogSkin.json"), textureAtlas);
		
		dialogBoxLabel = new Label("", skin, "dialogBox");
		dialogBoxLabel.setBounds(200, 0, scrnWidth-200, scrnHeight/4);
		dialogBoxLabel.setAlignment(Align.top | Align.left);
		dialogBoxLabel.setWrap(true);
		
		dialogNextLabel = new Label("", skin, "dialogNext");
		dialogNextLabel.setBounds(scrnWidth-50, 20, 32, 26);				
		
		interactLabel = new Label("Press [Enter] to interact", skin, "dialogBegin");
		float interactLabelWidth = interactLabel.getTextBounds().width;
		float interactLabelHeight = interactLabel.getTextBounds().height;
		interactLabel.setBounds(scrnWidth-(interactLabelWidth+50+10), scrnHeight-(interactLabelHeight*2+10), 
				interactLabelWidth+50, interactLabelHeight*2);
		interactLabel.setAlignment(Align.center);
		interactLabel.setWrap(true);
		
		imageStage = new Stage();
		
		float arrowSpacing = scrnWidth/50;
		float arrowW = scrnWidth/20;
		float arrowH = arrowW;
		arrow = new Image(new Sprite((Texture) assetManager.get("HUD/arrow2.png")));
		arrow.setBounds(arrowSpacing, scrnHeight - (arrowH+arrowSpacing), arrowW, arrowH);
		arrow.setOrigin(arrowW/2, arrowH/2);
		
		targetDistance = new Label("", skin, "distanceLabel");
		targetDistance.setFontScale(0.75f);
		targetDistance.setBounds(0, arrow.getY() - 30, 50, 10);
		
		addActor(targetDistance);
		addActor(arrow);
		addActor(dialogBoxLabel);
		addActor(dialogNextLabel);
		addActor(interactLabel);
		progressHUD=new QuestProgress(questManager.getNumQuests());

		RewardManager.initialize(questManager.getNumQuests(),
				questManager.getAvailableRewards());

		rewardLabel=new Label("Score: 0/"+RewardManager.getAvailableRewards(),skin,"dialogScreenLabel");
		rewardLabel.setBounds(20, 30, scrnWidth/6, scrnHeight/4);
		rewardLabel.setAlignment(Align.top | Align.left);
		rewardLabel.setWrap(false);
		questNameLabel=new Label("Current Quest: ",skin,"dialogScreenLabel");
		questNameLabel.setBounds(20, 55, scrnWidth/6, scrnHeight/8);
		questNameLabel.setAlignment(Align.top | Align.left);
		questNameLabel.setWrap(false);
		questNameLabel.setFontScale(0.6f);
		
	}
	
	public void resize(int width, int height){
		scrnWidth = width; 
		scrnHeight = height;
	}
	
	public void update(boolean inDial, boolean interAvail, NPC npc){
		inDialog = inDial;
		interactionAvailable = interAvail;
		if(npc != null){
			npcPortrait = new Image(npc.getNPCPortrait());
			npcPortrait.setBounds(0, 0, 200, 200);
		}	
		
		if(currentQuestIndex < questManager.getQuestIndex()){
			rewardLabel.setText("Score: " + RewardManager.getCurrentScore() + "/"+RewardManager.getAvailableRewards());
			currentQuestIndex = questManager.getQuestIndex();
		}
	}
	
	public void moveImages(int dir){
		if(imageStage.getActors().size > 0){
			float firstX = imageStage.getActors().first().getX();
			float lastX = imageStage.getActors().items[imageStage.getActors().size-1].getX();
			float width = imageStage.getActors().first().getWidth();
			
			Array<Actor> images = imageStage.getActors();
			
			if( (firstX < 0 && dir == 1) || (lastX + width > scrnWidth && dir == -1) ){
				for(int i = 0; i < images.size; i++){
					images.get(i).setX(images.get(i).getX() + (20 + width )*dir );
				}
			}
		}
		
	}
	
	public void addFaces(){
		//Should get the face sprites here.
		ArrayList<FaceWrapper> faces = questManager.getNodeFaces();
			
		if(faces != null && faces.size() > 0){
			Collections.shuffle(faces);
			
			int size = faces.size();
			float xMult = -1/(float)(size%2+1);
			float separate = 20;
			float sepMult = -(size-1)*0.5f;
			float imageAspectRatio = faces.get(0).getSpriteDrawable().getSprite().getWidth()/
					faces.get(0).getSpriteDrawable().getSprite().getHeight();
			float imageY = scrnHeight/3;
			float imageX = imageAspectRatio*imageY;
			
			if(size%2 == 1)
				xMult *= size;
			else if(size%2 == 0)
				xMult *= size/2;
			
			for(int i = 0; i < size; i++){
				Image temp = new Image(faces.get(i).getSpriteDrawable());
				
				float tempX = scrnWidth/2 + xMult*imageX + sepMult*separate;
				
				temp.setBounds(tempX, scrnHeight/2, imageX, imageY);
				imageStage.addActor(temp);
				
				xMult++;
				sepMult++;
			}
		}
		else{
			//System.out.println("Clear images");
			imageStage.clear();
		}
	}

}
