package propinquity;

import org.jbox2d.collision.shapes.CircleDef;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;

import processing.core.*;
import codeanticode.glgraphics.*;

public class Particle {

	public static final float SMALL_SIZE = 0.5f;
	public static final float LARGE_SIZE = 1f;

	public static final float METABALL_OVERSIZE_FACTOR = 1.5f;
	
	Vec2 position;
	Color color;

	float scale;
	Body body;
	CircleDef shape;
	PGraphics texture;

	Propinquity parent;

	boolean useOffscreen;

	public Particle(Propinquity parent, Vec2 position, Color color, float scale, boolean isNew) {
		this(parent, position, color, scale, isNew, true);
	}

	public Particle(Propinquity parent, Vec2 position, Color color, float scale, boolean isNew, boolean useOffscreen) {
		this.parent = parent;
		this.position = position;
		this.color = color;
		this.scale = scale;

		this.useOffscreen = useOffscreen;

		PImage imgParticle = parent.loadImage("data/particles/softparticle.png");
		texture = new PGraphics();
		texture = parent.createGraphics(imgParticle.width, imgParticle.height, PApplet.P2D);
		texture.background(imgParticle);
		// texture.mask(imgParticle);

		shape = new CircleDef();
		shape.radius = parent.box2d.scalarPixelsToWorld((texture.width - 22) * scale/2f);
		shape.density = 1.0f;
		shape.friction = 0.01f;
		shape.restitution = 0.3f;
		if(isNew) {
			shape.filter.categoryBits = Fences.CAT_NEW;
			shape.filter.maskBits = Fences.MASK_NEW;
		} else {
			shape.filter.categoryBits = Fences.CAT_OLD;
			shape.filter.maskBits = Fences.MASK_OLD;
		}

		BodyDef bd = new BodyDef();
		bd.position.set(parent.box2d.coordPixelsToWorld(position));

		body = parent.box2d.createBody(bd);
		body.createShape(shape);
		body.setMassFromShapes();
	}

	public float getScale() {
		return scale;
	}

	public Color getColor() {
		return color;
	}

	public Vec2 getPosition() {
		return position;
	}

	public void kill() {
		parent.box2d.destroyBody(body);
	}

	public Body getBody() {
		return body;
	}

	public CircleDef getCircleDef() {
		return shape;
	}

	public void draw() {
		position = parent.box2d.getBodyPixelCoord(body);

		if(useOffscreen) { //FIXME: Make each particle hold a GLOffscreen or PApplet
			GLGraphicsOffScreen offscreen = parent.getOffscreen();

			offscreen.beginDraw();

			offscreen.pushMatrix();
			offscreen.translate(position.x, position.y);
			offscreen.scale(METABALL_OVERSIZE_FACTOR * scale * texture.width/2f);
			offscreen.tint(color.toInt(parent));
			offscreen.image(texture, -1, -1, 2, 2);
			offscreen.noTint();
			offscreen.popMatrix();

			offscreen.endDraw();
		} else {
			parent.pushMatrix();
			parent.translate(position.x, position.y);
			parent.scale(scale * texture.width/2f);
			parent.beginShape();
			parent.texture(texture);
			parent.tint(color.toInt(parent));
			parent.vertex(-1, -1, 0, 0, 0);
			parent.vertex(1, -1, 0, 1, 0);
			parent.vertex(1, 1, 0, 1, 1);
			parent.vertex(-1, 1, 0, 0, 1);
			parent.noTint();
			parent.endShape();
			parent.popMatrix();
		}
	}
}
