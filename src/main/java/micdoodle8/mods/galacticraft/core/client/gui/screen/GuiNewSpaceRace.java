package micdoodle8.mods.galacticraft.core.client.gui.screen;

import java.util.ArrayList;
import java.util.List;

import micdoodle8.mods.galacticraft.api.vector.Vector3;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.client.gui.element.GuiCheckbox;
import micdoodle8.mods.galacticraft.core.client.gui.element.GuiCheckbox.ICheckBoxCallback;
import micdoodle8.mods.galacticraft.core.client.model.ModelFlag;
import micdoodle8.mods.galacticraft.core.client.render.item.ItemRendererFlag;
import micdoodle8.mods.galacticraft.core.dimension.SpaceRaceManager;
import micdoodle8.mods.galacticraft.core.entities.EntityFlag;
import micdoodle8.mods.galacticraft.core.network.PacketSimple;
import micdoodle8.mods.galacticraft.core.network.PacketSimple.EnumSimplePacket;
import micdoodle8.mods.galacticraft.core.util.CoreUtil;
import micdoodle8.mods.galacticraft.core.util.EnumColor;
import micdoodle8.mods.galacticraft.core.wrappers.FlagData;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLLog;

public class GuiNewSpaceRace extends GuiScreen implements ICheckBoxCallback
{
	protected static final ResourceLocation texture = new ResourceLocation(GalacticraftCore.ASSET_DOMAIN, "textures/gui/gui.png");
	
	public static enum EnumSpaceRaceGui
	{
		MAIN,
		RULES,
		DESIGN_FLAG
	}
	
    private int ticksPassed;
    private EntityPlayer thePlayer;
	private GuiCheckbox checkboxCompete;
	private GuiCheckbox checkboxUploadScore;
	private boolean initialized;
	private EnumSpaceRaceGui currentState = EnumSpaceRaceGui.MAIN;

    private int buttonBack_width;
    private int buttonBack_height;
    private int buttonBack_xPosition;
    private int buttonBack_yPosition;
    private boolean buttonBack_hover;
    
    private int buttonRules_width;
    private int buttonRules_height;
    private int buttonRules_xPosition;
    private int buttonRules_yPosition;
    private boolean buttonRules_hover;
    
    private int buttonDone_width;
    private int buttonDone_height;
    private int buttonDone_xPosition;
    private int buttonDone_yPosition;
    private boolean buttonDone_hover;
    
    private int buttonFlag_width;
    private int buttonFlag_height;
    private int buttonFlag_xPosition;
    private int buttonFlag_yPosition;
    private boolean buttonFlag_hover;
    
    private boolean optionCompete = true;
    private boolean optionUpload = true;

	private EntityFlag dummyFlag = new EntityFlag(FMLClientHandler.instance().getClient().theWorld);
	private ModelFlag dummyModel = new ModelFlag();
	
	private FlagData flagData = new FlagData(48, 32);
    
	public GuiNewSpaceRace(EntityPlayer player)
	{
		this.thePlayer = player;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui()
	{
		super.initGui();
		this.buttonList.clear();
		
		if (this.initialized)
		{
			final int var5 = (this.width - this.width / 4) / 2;
			final int var6 = (this.height - this.height / 4) / 2;
			
			this.buttonBack_xPosition = this.width / 2 - this.width / 3 + 15;
			this.buttonBack_yPosition = this.height / 2 - this.height / 4 - 15;
			this.buttonBack_width = 50;
			this.buttonBack_height = 15;

			this.buttonRules_width = 50;
			this.buttonRules_xPosition = this.width / 2 + this.width / 3 - 75 - buttonRules_width;
			this.buttonRules_yPosition = this.height / 2 + this.height / 4 - 25;
			this.buttonRules_height = 15;

			this.buttonDone_width = 50;
			this.buttonDone_xPosition = this.buttonRules_xPosition + buttonRules_width + 10;
			this.buttonDone_yPosition = this.height / 2 + this.height / 4 - 25;
			this.buttonDone_height = 15;

			this.buttonFlag_width = 78;
			this.buttonFlag_height = 31;
			this.buttonFlag_xPosition = this.width / 2 - buttonFlag_width / 2 + 50;
			this.buttonFlag_yPosition = this.height / 2 - buttonFlag_height / 2 + 3;
			
			switch (this.currentState)
			{
			case MAIN:
				this.checkboxCompete = new GuiCheckbox(0, this, this.width / 2 - 125, var6 - 10, "Compete against other players on this world?");
				this.checkboxUploadScore = new GuiCheckbox(1, this, this.width / 2 - 125, var6 + 5, "Upload score to Galacticraft website?");
				this.buttonList.add(this.checkboxCompete);
				this.buttonList.add(this.checkboxUploadScore);
				break;
			case RULES:
				break;
			case DESIGN_FLAG:
				break;
			default:
				break;
			}
		}
		else
		{
			for (int i = 0; i < this.flagData.getWidth(); i++)
			{
				for (int j = 0; j < this.flagData.getHeight(); j++)
				{
					this.flagData.setColorAt(i, j, new Vector3(255, 100, 100));
				}
			}
			
			this.flagData.setColorAt(0, 0, new Vector3(50, 100, 255));
			this.flagData.setColorAt(0, this.flagData.getHeight() - 1, new Vector3(50, 100, 255));
			this.flagData.setColorAt(this.flagData.getWidth() - 1, 0, new Vector3(50, 100, 100));
			this.flagData.setColorAt(this.flagData.getWidth() - 1, this.flagData.getHeight() - 1, new Vector3(50, 100, 255));
		}
	}

    protected void mouseClicked(int x, int y, int clickIndex)
    {
    	super.mouseClicked(x, y, clickIndex);
    	
        if (clickIndex == 0)
        {
        	if (this.buttonBack_hover)
        	{
        		if (this.currentState == EnumSpaceRaceGui.MAIN)
        		{
        			this.thePlayer.closeScreen();
        		}
        		else
        		{
            		this.currentState = EnumSpaceRaceGui.MAIN;
            		this.initGui();
        		}
        	}
        	else if (this.buttonRules_hover)
        	{
        		this.currentState = EnumSpaceRaceGui.RULES;
        		this.initGui();
        	}
        	else if (this.buttonDone_hover)
        	{
        		List<Object> objList = new ArrayList<Object>();
        		objList.add("NewTeam");
        		objList.add(this.flagData);
        		objList.add(new String[] { this.thePlayer.getGameProfile().getName() });
        		GalacticraftCore.packetPipeline.sendToServer(new PacketSimple(EnumSimplePacket.S_START_NEW_SPACE_RACE, objList));
        		List<String> players = new ArrayList<String>();
        		players.add(this.thePlayer.getGameProfile().getName());
        		SpaceRaceManager.addSpaceRace(players, "NewTeam", flagData);
        		this.thePlayer.closeScreen();
        	}
        	else if (this.buttonFlag_hover)
        	{
        		this.currentState = EnumSpaceRaceGui.DESIGN_FLAG;
        		this.initGui();
        	}
        }
    }

    public void updateScreen()
    {
        super.updateScreen();
        ++this.ticksPassed;
    }
    
    public void drawScreen(int par1, int par2, float par3)
    {
        this.drawDefaultBackground();
		final int var5 = (this.width - this.width / 4) / 2;
		final int var6 = (this.height - this.height / 4) / 2;
		
		if (this.initialized)
		{
	        switch (this.currentState)
	        {
	        case MAIN:
				this.drawCenteredString(this.fontRendererObj, "New Space Race", this.width / 2, var6 - 25, 16777215);
				this.drawRulesButton(par1, par2);
	        	this.drawBackButton(par1, par2);
	        	this.drawDoneButton(par1, par2);
	        	this.drawFlagButton(par1, par2);
	    		String rememberStr = EnumColor.RED + "Remember: You're competing against time spent in-game before reaching space, not who gets to space first!";
	    		int trimWidth = this.width / 4 + 65;
	            List list2 = this.fontRendererObj.listFormattedStringToWidth(rememberStr, trimWidth);
	    		this.fontRendererObj.drawSplitString(rememberStr, this.width / 2 - this.width / 3 + 7, this.height / 2 + this.height / 4 - list2.size() * this.fontRendererObj.FONT_HEIGHT - 5, trimWidth, CoreUtil.to32BitColor(255, 100, 100, 100));
	        	break;
	        case RULES:
				this.drawCenteredString(this.fontRendererObj, "Rules", this.width / 2, var6 - 25, 16777215);
	        	this.drawBackButton(par1, par2);
	        	break;
	        case DESIGN_FLAG:
				this.drawCenteredString(this.fontRendererObj, "Design New Flag", this.width / 2, var6 - 31, 16777215);
	        	this.drawBackButton(par1, par2);
	        	float scaleX = this.width / 130.0F;
	        	float scaleY = this.height / 75.0F;
	        	float baseX = this.width / 2 - (this.flagData.getWidth() * scaleX) / 2;
	        	float baseY = this.height / 2 - (this.flagData.getHeight() * scaleY) / 2 + 3;
	        	
	        	for (int x = 0; x < this.flagData.getWidth(); x++)
	        	{
	        		for (int y = 0; y < this.flagData.getHeight(); y++)
	        		{
	        			Vector3 color = this.flagData.getColorAt(x, y);
	        	        Tessellator tessellator = Tessellator.instance;
	        	        GL11.glEnable(GL11.GL_BLEND);
	        	        GL11.glDisable(GL11.GL_TEXTURE_2D);
	        	        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
	        	        GL11.glColor4f(color.floatX(), color.floatY(), color.floatZ(), 1.0F);
	        	        tessellator.startDrawingQuads();
	        	        tessellator.addVertex((double)baseX + x * scaleX, (double)baseY + y * scaleY + scaleY / 1.1, 0.0D);
	        	        tessellator.addVertex((double)baseX + x * scaleX + scaleX / 1.1, (double)baseY + y * scaleY + scaleY / 1.1, 0.0D);
	        	        tessellator.addVertex((double)baseX + x * scaleX + scaleX / 1.1, (double)baseY + y * scaleY, 0.0D);
	        	        tessellator.addVertex((double)baseX + x * scaleX, (double)baseY + y * scaleY, 0.0D);
	        	        tessellator.draw();
	        	        GL11.glEnable(GL11.GL_TEXTURE_2D);
	        	        GL11.glDisable(GL11.GL_BLEND);
	        		}
	        	}
	        	
	        	float x1 = 0;
	        	float x2 = 100;
	        	float y1 = 0;
	        	float y2 = 100;
	        	GL11.glDisable(GL11.GL_TEXTURE_2D);
	            GL11.glEnable(GL11.GL_BLEND);
	            GL11.glDisable(GL11.GL_ALPHA_TEST);
	            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
	            GL11.glShadeModel(GL11.GL_SMOOTH);
	            Tessellator tessellator = Tessellator.instance;
                
	            tessellator.startDrawing(GL11.GL_TRIANGLE_FAN);
	            tessellator.setColorRGBA_F(1,0,0, 1);
	            tessellator.addVertex(100, 100, (double)this.zLevel);
		    	
//		    	for (int i = 0; i < 180; i++)
//		    	{
//		    		float x = (float) (Math.sin(i) * 100) + 100;
//		    		float y = (float) (Math.cos(i) * 100) + 100;
//		            tessellator.addVertex(x, y, (double)this.zLevel);
//		    		x = (float) (Math.sin(i + 0.1F) * 100) + 100;
//		    		y = (float) (Math.cos(i + 0.1F) * 100) + 100;
//		            tessellator.addVertex(x, y, (double)this.zLevel);
//		    	}
		    	
		    	int i;
		    	double sections = 180;
		    	float radius = 100.1F;
		    	float twoPi = 2.0F * 3.14159F;

//		    	tessellator.addVertex(0.0, 0.0, (double)this.zLevel); //center of triangles

		    	for(i = 0; i <= sections;i++) {

		    		tessellator.addVertex(radius*Math.sin(i) + 100,
		    	               radius*Math.cos(i) + 100, (double)this.zLevel);
		    		tessellator.addVertex(radius*Math.sin(i + 0.1F) + 100,
		    	               radius*Math.cos(i + 0.1F) + 100, (double)this.zLevel);

		    	    if(i%6 == 0)
		    	    	tessellator.setColorRGBA_F(1.0f, 0.0f, 0.0f, 1);

		    	    if (i%6 == 1)
		    	    	tessellator.setColorRGBA_F(1.0f, 0.5f, 0.0f, 1);

		    	    if (i%6 == 2)
		    	    	tessellator.setColorRGBA_F(1.0f, 1.0f, 0.0f, 1);

		    	    if (i%6 == 3)
		    	    	tessellator.setColorRGBA_F(0.0f, 1.0f, 0.0f, 1);

		    	    if (i%6 == 4)
		    	    	tessellator.setColorRGBA_F(0.0f, 0.0f, 1.0f, 1);

		    	    if (i%6 == 5)
		    	    	tessellator.setColorRGBA_F(1.0f, 0.0f, 1.0f, 1);
		    	}
		    	
//		    	for(int i = 0; i <= 180; i++) 
//		    	{
//		    	    if(i%6 == 0)
//		    	    	tessellator.setColorRGBA_F(1.0f, 0.0f, 0.0f, 1);
//
//		    	    if (i%6 == 1)
//		    	    	tessellator.setColorRGBA_F(1.0f, 0.5f, 0.0f, 1);
//
//		    	    if (i%6 == 2)
//		    	    	tessellator.setColorRGBA_F(1.0f, 1.0f, 0.0f, 1);
//
//		    	    if (i%6 == 3)
//		    	    	tessellator.setColorRGBA_F(0.0f, 1.0f, 0.0f, 1);
//
//		    	    if (i%6 == 4)
//		    	    	tessellator.setColorRGBA_F(0.0f, 0.0f, 1.0f, 1);
//
//		    	    if (i%6 == 5)
//		    	    	tessellator.setColorRGBA_F(1.0f, 0.0f, 1.0f, 1);
//
//		    	    FMLLog.info("" + (100 * Math.cos(i * (2 * Math.PI) / 6.0F) + 100));
//		    	    
//		            tessellator.addVertex(100 * Math.cos(i * (2 * Math.PI) / 6.0F) + 100,
//		    	    		100 * Math.sin(i * (2 * Math.PI) / 6.0F) + 100, (double)this.zLevel);
//		    	}
		    	
//	            tessellator.setColorRGBA_F(0, 0, 1, 1);
//	            tessellator.addVertex((double)x2, (double)y1, (double)this.zLevel);
//	            tessellator.setColorRGBA_F(0, 0, 0, 1);
//	            tessellator.addVertex((double)x1, (double)y1, (double)this.zLevel);
//	            tessellator.setColorRGBA_F(1, 0, 0, 1);
//	            tessellator.addVertex((double)x1, (double)y2, (double)this.zLevel);
//	            tessellator.setColorRGBA_F(0, 1, 0, 1);
//	            tessellator.addVertex((double)x2, (double)y2, (double)this.zLevel);
	            tessellator.draw();
	            
	            tessellator.startDrawingQuads();
	            
//	            int i = 0;
//	            for (float yScale = y1; yScale < y2; yScale += (y2 - y1) / 6.0F)
//	            {
//		            switch (i)
//		            {
//		            case 0:
//			            tessellator.setColorRGBA_F(1, 0, 0, 1);
//		            	break;
//		            case 1:
//			            tessellator.setColorRGBA_F(1, 1, 1, 1);
//		            	break;
//		            case 2:
//			            tessellator.setColorRGBA_F(1, 1, 1, 1);
//		            	break;
//		            case 3:
//			            tessellator.setColorRGBA_F(1, 1, 1, 1);
//		            	break;
//		            case 4:
//			            tessellator.setColorRGBA_F(1, 1, 1, 1);
//		            	break;
//		            case 5:
//			            tessellator.setColorRGBA_F(1, 1, 1, 1);
//		            	break;
//		            case 6:
//			            tessellator.setColorRGBA_F(1, 1, 1, 1);
//		            	break;
//		            }
//		            
//		            if (i % 2 == 0)
//		            {
//			            tessellator.addVertex((double)x2 + 30, (double)yScale, (double)this.zLevel);
//			            tessellator.addVertex((double)x2 + 10, (double)yScale, (double)this.zLevel);
//		            }
//		            else
//		            {
//			            tessellator.addVertex((double)x2 + 10, (double)yScale + (y2 - y1) / 6.0F, (double)this.zLevel);
//			            tessellator.addVertex((double)x2 + 30, (double)yScale + (y2 - y1) / 6.0F, (double)this.zLevel);
//		            }
//		            
//		            i++;
//	            }
	            
	            tessellator.draw();
	            
	            GL11.glShadeModel(GL11.GL_FLAT);
	            GL11.glDisable(GL11.GL_BLEND);
	            GL11.glEnable(GL11.GL_ALPHA_TEST);
	            GL11.glEnable(GL11.GL_TEXTURE_2D);
	        	
	        	break;
	        }
		}
		
        super.drawScreen(par1, par2, par3);
    }
    
    private void drawFlagButton(int mouseX, int mouseY)
    {
		GL11.glPushMatrix();
        GL11.glTranslatef(this.buttonFlag_xPosition + this.buttonFlag_width / 2 - 22, this.buttonFlag_yPosition + this.buttonFlag_height / 2 + 22, -2.0F + zLevel);
        GL11.glScalef(35F, 35F, 35F);
        GL11.glTranslatef(0.0F, 0.0F, 1.0F);
        GL11.glScalef(1.0F, 1.0F, -1F);
		FMLClientHandler.instance().getClient().renderEngine.bindTexture(ItemRendererFlag.flagTextures[5]);
		this.dummyFlag.flagData = this.flagData;
		this.dummyModel.renderFlag(this.dummyFlag, 0.0625F);
		GL11.glColor3f(1, 1, 1);
		this.dummyModel.renderFace(this.dummyFlag, 0.0625F, true);
		GL11.glPopMatrix();

		GL11.glPushMatrix();
        GL11.glTranslatef(0.0F, 0.0F, 500.0F);
        this.drawCenteredString(this.fontRendererObj, EnumColor.ORANGE + "Customize Flag", this.buttonFlag_xPosition + this.buttonFlag_width / 2, this.buttonFlag_yPosition + this.buttonFlag_height / 2 - 5, CoreUtil.to32BitColor(255, 100, 100, 100));
        GL11.glPopMatrix();

		this.buttonFlag_hover = mouseX >= this.buttonFlag_xPosition && mouseY >= this.buttonFlag_yPosition && mouseX < this.buttonFlag_xPosition + this.buttonFlag_width && mouseY < this.buttonFlag_yPosition + this.buttonFlag_height;
		
        if (this.buttonFlag_hover)
        {
            this.drawRect(this.buttonFlag_xPosition, this.buttonFlag_yPosition, buttonFlag_xPosition + buttonFlag_width, buttonFlag_yPosition + buttonFlag_height, CoreUtil.to32BitColor(255, 50, 50, 50));
        }
        
        this.drawRect(this.buttonFlag_xPosition + buttonFlag_width - 1, this.buttonFlag_yPosition, buttonFlag_xPosition + buttonFlag_width, buttonFlag_yPosition + buttonFlag_height, CoreUtil.to32BitColor(255, 0, 0, 0));
        this.drawRect(this.buttonFlag_xPosition, this.buttonFlag_yPosition, buttonFlag_xPosition + 1, buttonFlag_yPosition + buttonFlag_height, CoreUtil.to32BitColor(255, 0, 0, 0));
        this.drawRect(this.buttonFlag_xPosition, this.buttonFlag_yPosition, buttonFlag_xPosition + buttonFlag_width, buttonFlag_yPosition + 1, CoreUtil.to32BitColor(255, 0, 0, 0));
        this.drawRect(this.buttonFlag_xPosition, buttonFlag_yPosition + buttonFlag_height - 1, buttonFlag_xPosition + buttonFlag_width, buttonFlag_yPosition + buttonFlag_height, CoreUtil.to32BitColor(255, 0, 0, 0));
    }
    
    private void drawBackButton(int mouseX, int mouseY)
    {
		this.buttonBack_hover = mouseX >= this.buttonBack_xPosition && mouseY >= this.buttonBack_yPosition && mouseX < this.buttonBack_xPosition + this.buttonBack_width && mouseY < this.buttonBack_yPosition + this.buttonBack_height;
        this.drawGradientRect(this.buttonBack_xPosition, this.buttonBack_yPosition, this.buttonBack_xPosition + this.buttonBack_width, this.buttonBack_yPosition + this.buttonBack_height, buttonBack_hover ? CoreUtil.to32BitColor(150, 30, 30, 30) : CoreUtil.to32BitColor(150, 10, 10, 10), buttonBack_hover ? CoreUtil.to32BitColor(250, 30, 30, 30) : CoreUtil.to32BitColor(250, 10, 10, 10));
        this.drawCenteredString(this.fontRendererObj, this.currentState == EnumSpaceRaceGui.MAIN ? "Close" : "Back", this.buttonBack_xPosition + this.buttonBack_width / 2, this.buttonBack_yPosition + this.buttonBack_height / 4, CoreUtil.to32BitColor(255, 100, 100, 100));
    }
    
    private void drawRulesButton(int mouseX, int mouseY)
    {
		this.buttonRules_hover = mouseX >= this.buttonRules_xPosition && mouseY >= this.buttonRules_yPosition && mouseX < this.buttonRules_xPosition + this.buttonRules_width && mouseY < this.buttonRules_yPosition + this.buttonRules_height;
        this.drawGradientRect(this.buttonRules_xPosition, this.buttonRules_yPosition, this.buttonRules_xPosition + this.buttonRules_width, this.buttonRules_yPosition + this.buttonRules_height, buttonRules_hover ? CoreUtil.to32BitColor(150, 30, 30, 30) : CoreUtil.to32BitColor(150, 10, 10, 10), buttonRules_hover ? CoreUtil.to32BitColor(250, 30, 30, 30) : CoreUtil.to32BitColor(250, 10, 10, 10));
        this.drawCenteredString(this.fontRendererObj, "Rules", this.buttonRules_xPosition + this.buttonRules_width / 2, this.buttonRules_yPosition + this.buttonRules_height / 4, CoreUtil.to32BitColor(255, 100, 100, 100));
    }
    
    private void drawDoneButton(int mouseX, int mouseY)
    {
		this.buttonDone_hover = mouseX >= this.buttonDone_xPosition && mouseY >= this.buttonDone_yPosition && mouseX < this.buttonDone_xPosition + this.buttonDone_width && mouseY < this.buttonDone_yPosition + this.buttonDone_height;
        this.drawGradientRect(this.buttonDone_xPosition, this.buttonDone_yPosition, this.buttonDone_xPosition + this.buttonDone_width, this.buttonDone_yPosition + this.buttonDone_height, buttonDone_hover ? CoreUtil.to32BitColor(150, 30, 30, 30) : CoreUtil.to32BitColor(150, 10, 10, 10), buttonDone_hover ? CoreUtil.to32BitColor(250, 30, 30, 30) : CoreUtil.to32BitColor(250, 10, 10, 10));
        this.drawCenteredString(this.fontRendererObj, "GO!", this.buttonDone_xPosition + this.buttonDone_width / 2, this.buttonDone_yPosition + this.buttonDone_height / 4, CoreUtil.to32BitColor(255, 100, 100, 100));
    }

    public void drawWorldBackground(int i)
    {
        if (this.mc.theWorld != null)
        {
        	int scaleX = Math.min(ticksPassed * 4, this.width / 3);
        	int scaleY = Math.min(ticksPassed * 4, this.height / 4);
        	
        	if (scaleX == this.width / 3 && scaleY == this.height / 4 && !this.initialized)
        	{
        		this.initialized = true;
        		this.initGui();
        	}
        	
            this.drawGradientRect(this.width / 2 - scaleX, this.height / 2 - scaleY, this.width / 2 + scaleX, this.height / 2 + scaleY, -1072689136, -804253680);
        }
        else
        {
            this.drawBackground(i);
        }
    }

	@Override
	public void onSelectionChanged(GuiCheckbox checkbox, boolean newSelected)
	{
		if (checkbox.equals(this.checkboxCompete))
		{
			this.optionCompete = newSelected;
		}
		else if (checkbox.equals(this.checkboxUploadScore))
		{
			this.optionUpload = newSelected;
		}
	}

	@Override
	public boolean canPlayerEdit(GuiCheckbox checkbox, EntityPlayer player)
	{
		return true;
	}

	@Override
	public boolean getInitiallySelected(GuiCheckbox checkbox)
	{
		if (checkbox.equals(this.checkboxCompete))
		{
			return this.optionCompete;
		}
		else if (checkbox.equals(this.checkboxUploadScore))
		{
			return this.optionUpload;
		}
		
		return false;
	}

	@Override
	public void onIntruderInteraction()
	{
		;
	}
}